package io.tomahawkd.tlstester.config;

import com.beust.jcommander.Parameter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

public class MiscArgDelegate extends AbstractArgDelegate {

	@Parameter(names = "--debug", description = "Show debug output (sets logLevel to DEBUG)")
	private boolean debug;

	@Parameter(names = "--quiet", description = "No output (sets logLevel to NONE)")
	private boolean quiet;

	@Parameter(names = "--temp", description = "Temp file expired day. (-1 indicates forever)")
	private Integer tempExpireTime = 7;

	public boolean isDebug() {
		return debug;
	}

	public boolean isQuiet() {
		return quiet;
	}

	public Integer getTempExpireTime() {
		return tempExpireTime;
	}

	@Override
	public void postParsing() {
		if (debug) {
			Configurator.setAllLevels("io.tomahawkd.tlstester", Level.DEBUG);
		} else if (quiet) {
			Configurator.setAllLevels("io.tomahawkd.tlstester", Level.OFF);
		}
	}
}
