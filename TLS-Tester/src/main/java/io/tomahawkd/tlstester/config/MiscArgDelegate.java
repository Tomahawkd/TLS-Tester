package io.tomahawkd.tlstester.config;

import com.beust.jcommander.Parameter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.util.List;

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
			LoggerContext ctx = LoggerContext.getContext(false);
			Configuration config = ctx.getConfiguration();
			LoggerConfig loggerConfig = config.getLoggerConfig("io.tomahawkd.tlstester");
			loggerConfig.removeAppender("Console");
			loggerConfig.addAppender(
					config.getAppender("DebugConsole"), Level.DEBUG, null);
			ctx.updateLoggers();
		} else if (quiet) {
			Configurator.setAllLevels("io.tomahawkd.tlstester", Level.OFF);
		}
	}
}
