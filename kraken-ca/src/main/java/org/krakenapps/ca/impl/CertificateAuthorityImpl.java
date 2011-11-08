/*
 * Copyright 2009 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.ca.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.krakenapps.api.Primitive;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.ca.CertificateAuthority;
import org.krakenapps.ca.CertificateMetadata;
import org.krakenapps.ca.CertificateRequest;
import org.krakenapps.ca.RevocationReason;
import org.krakenapps.ca.RevokedCertificate;
import org.krakenapps.ca.util.CertificateBuilder;
import org.krakenapps.ca.util.CertificateExporter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.confdb.file.FileConfigDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See {@link http://www.bouncycastle.org/wiki/display/JA1/Home}
 * 
 * @author xeraph
 * 
 */
public class CertificateAuthorityImpl implements CertificateAuthority {
	private final Logger logger = LoggerFactory.getLogger(CertificateAuthorityImpl.class.getName());
	private static final String[] sigAlgorithms = new String[] { "MD5withRSA", "MD5withRSA", "SHA1withRSA",
			"SHA224withRSA", "SHA256withRSA", "SHA384withRSA", "SHA512withRSA" };

	private ConfigDatabase db;
	private String name;

	public CertificateAuthorityImpl(ConfigDatabase db, String name) {
		this.db = db;
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public CertificateMetadata getRootCertificate() {
		ConfigCollection metadata = db.ensureCollection("metadata");
		Config jks = metadata.findOne(Predicates.field("type", "jks"));
		if (jks == null)
			throw new IllegalStateException("jks not found for " + name);

		return PrimitiveConverter.parse(CertificateMetadata.class, jks.getDocument());
	}

	@Override
	public List<CertificateMetadata> getCertificates() {
		ConfigCollection certs = db.ensureCollection("certs");

		List<CertificateMetadata> l = new LinkedList<CertificateMetadata>();
		ConfigIterator it = certs.findAll();
		try {
			while (it.hasNext()) {
				Config c = it.next();
				CertificateMetadata cm = PrimitiveConverter.parse(CertificateMetadata.class, c.getDocument());
				l.add(cm);
			}
		} finally {
			it.close();
		}
		return l;
	}

	@Override
	public BigInteger getLastSerial() {
		ConfigCollection col = db.ensureCollection("metadata");
		Config c = col.findOne(Predicates.field("type", "serial"));
		if (c == null)
			return new BigInteger("1");

		return new BigInteger((String) c.getDocument());
	}

	@Override
	public BigInteger getNextSerial() {
		ConfigCollection col = db.ensureCollection("metadata");
		Config c = col.findOne(Predicates.field("type", "serial"));
		if (c == null) {
			Object doc = PrimitiveConverter.serialize(new CertSerial());
			col.add(doc, "kraken-ca", "init serial");
			return new BigInteger("2");
		}

		CertSerial s = PrimitiveConverter.parse(CertSerial.class, c.getDocument());
		s.serial = new BigInteger(s.serial).add(new BigInteger("1")).toString();
		c.setDocument(PrimitiveConverter.serialize(s));
		col.update(c, false, "kraken-ca", "set next serial");
		return new BigInteger(s.serial);
	}

	@Override
	public CertificateMetadata findCertificate(String field, String value) {
		ConfigCollection certs = db.ensureCollection("certs");
		Config c = certs.findOne(Predicates.field(field, value));
		return PrimitiveConverter.parse(CertificateMetadata.class, c.getDocument());

	}

	@SuppressWarnings("unchecked")
	@Override
	public byte[] getPfxBinary(String subjectDn, String keyPassword) {
		ConfigCollection col = db.ensureCollection("certs");
		Config c = col.findOne(Predicates.field("subject_dn", subjectDn));
		if (c == null)
			return null;

		Map<String, Object> doc = (Map<String, Object>) c.getDocument();
		return Base64.decodeBase64((String) doc.get("binary"));
	}

	@Override
	public CertificateMetadata issueCertificate(String caPassword, CertificateRequest req) throws Exception {
		ConfigCollection metadata = db.ensureCollection("metadata");
		Config jks = metadata.findOne(Predicates.field("type", "jks"));
		if (jks == null)
			throw new IllegalStateException("ca not found for " + name);

		CertificateMetadata cm = PrimitiveConverter.parse(CertificateMetadata.class, jks.getDocument());
		req.setSerial(getNextSerial());
		req.setIssuerDn(cm.getSubjectDn());
		req.setIssuerKey(cm.getPrivateKey(caPassword));

		// check availability of signature algorithm
		validateSignatureAlgorithm(req.getSignatureAlgorithm());

		// generate cert
		X509Certificate caCert = cm.getCertificate(caPassword);
		X509Certificate cert = CertificateBuilder.createCertificate(req);
		byte[] pkcs12 = CertificateExporter.exportPkcs12(cert, req.getKeyPair(), req.getKeyPassword(), caCert);

		cm = new CertificateMetadata();
		cm.setType("pkcs12");
		cm.setSerial(req.getSerial().toString());
		cm.setSubjectDn(req.getSubjectDn());
		cm.setNotBefore(req.getNotBefore());
		cm.setNotAfter(req.getNotAfter());
		cm.setBinary(Base64.encodeBase64String(pkcs12));

		ConfigCollection certs = db.ensureCollection("certs");
		Object c = PrimitiveConverter.serialize(cm);
		certs.add(c, "kraken-ca", "issued certificate for " + req.getSubjectDn());

		logger.info("kraken ca: generated new certificate [{}]", cert.getSubjectX500Principal().getName());
		return cm;
	}

	public static void validateSignatureAlgorithm(String algorithm) {
		for (int i = 0; i < sigAlgorithms.length; i++)
			if (sigAlgorithms[i].equals(algorithm))
				return;

		throw new IllegalArgumentException("invalid signature algorithm: " + algorithm);
	}

	@Override
	public void revoke(CertificateMetadata cm) {
		revoke(cm, RevocationReason.Unspecified);
	}

	public void revoke(CertificateMetadata cm, RevocationReason reason) {
		ConfigCollection revoked = db.ensureCollection("revoked");
		Config c = revoked.findOne(Predicates.field("serial", cm.getSerial()));
		if (c != null)
			throw new IllegalStateException("already revoked: serial " + cm.getSerial());

	}

	@Override
	public List<RevokedCertificate> getRevokedCertifcates() {
		ConfigCollection revoked = db.ensureCollection("revoked");
		ConfigIterator it = revoked.findAll();
		List<RevokedCertificate> l = new LinkedList<RevokedCertificate>();

		try {
			while (it.hasNext()) {
				Config c = it.next();
				RevokedCertificate rc = PrimitiveConverter.parse(RevokedCertificate.class, c.getDocument());
				l.add(rc);
			}
		} finally {
			it.close();
		}

		return l;
	}

	@Override
	public String toString() {
		return name + ": " + getRootCertificate().getSubjectDn();
	}

	private static class CertSerial {
		private String type = "serial";
		private String serial = "2";

		public CertSerial() {
		}
	}
}