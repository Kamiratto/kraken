package org.krakenapps.logparser.syslog.hp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.log.api.LogParser;
import org.krakenapps.log.api.LogParserFactory;
import org.krakenapps.log.api.LoggerConfigOption;

/**
 * 
 * @author xeraph
 * @since 1.4.0
 */
@Component(name = "tippingpoint-log-parser-factory")
@Provides
public class TippingPointSmsLogParserFactory implements LogParserFactory {

	@Override
	public String getName() {
		return "tippingpoint-sms";
	}

	@Override
	public Collection<Locale> getDisplayNameLocales() {
		return Arrays.asList(Locale.ENGLISH);
	}

	@Override
	public String getDisplayName(Locale locale) {
		return "tippingpoint log parser";
	}

	@Override
	public Collection<Locale> getDescriptionLocales() {
		return Arrays.asList(Locale.ENGLISH);
	}

	@Override
	public String getDescription(Locale locale) {
		return "tippingpoint log parser";
	}

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		return new ArrayList<LoggerConfigOption>();
	}

	@Override
	public LogParser createParser(Properties config) {
		return new TippingPointSmsLogParser();
	}

}
