package org.krakenapps.confdb.file;

import java.nio.ByteBuffer;

import org.krakenapps.confdb.CommitOp;

public class CollectionLog {
	/**
	 * revision of database for conflict check (8byte)
	 */
	private long rev;

	/**
	 * previous revision of database for branching (8byte)
	 */
	private long prevRev;

	/**
	 * operation code (MSB 1byte)
	 */
	private CommitOp operation;

	/**
	 * unique incremental id in collection (4byte)
	 */
	private int docId;

	/**
	 * data file offset (8byte)
	 */
	private long docOffset;

	/**
	 * document length (4byte)
	 */
	private int docLength;

	/**
	 * raw doc binary
	 */
	private byte[] doc;

	public long getRev() {
		return rev;
	}

	public void setRev(long rev) {
		this.rev = rev;
	}

	public long getPrevRev() {
		return prevRev;
	}

	public void setPrevRev(long prevRev) {
		this.prevRev = prevRev;
	}

	public CommitOp getOperation() {
		return operation;
	}

	public void setOperation(CommitOp operation) {
		this.operation = operation;
	}

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	public long getDocOffset() {
		return docOffset;
	}

	public void setDocOffset(long docOffset) {
		this.docOffset = docOffset;
	}

	public int getDocLength() {
		return docLength;
	}

	public void setDocLength(int docLength) {
		this.docLength = docLength;
	}

	public byte[] getDoc() {
		return doc;
	}

	public void setDoc(byte[] doc) {
		this.doc = doc;
	}

	public void serialize(ByteBuffer bb) {
		// total 34 bytes
		bb.putLong(rev);
		bb.putLong(prevRev);
		bb.put((byte) operation.getCode());
		bb.put((byte) 0);
		bb.putInt(docId);
		bb.putLong(docOffset);
		bb.putInt((int) docLength);
		bb.flip();
	}

	public static CollectionLog deserialize(ByteBuffer bb) {
		CollectionLog log = new CollectionLog();
		log.setRev(bb.getLong());
		log.setPrevRev(bb.getLong());
		log.setOperation(CommitOp.parse(bb.get()));
		bb.get(); // padding
		log.setDocId(bb.getInt());
		log.setDocOffset(bb.getLong());
		log.setDocLength(bb.getInt());
		return log;
	}

	@Override
	public String toString() {
		return "rev=" + rev + ", prev=" + prevRev + ", op=" + operation + ", doc=" + docId + ", len=" + docLength;
	}

}
