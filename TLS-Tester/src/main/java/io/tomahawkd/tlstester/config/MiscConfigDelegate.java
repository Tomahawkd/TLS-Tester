package io.tomahawkd.tlstester.config;

import com.beust.jcommander.Parameter;
import io.tomahawkd.config.AbstractConfigDelegate;
import io.tomahawkd.config.annotation.BelongsTo;
import io.tomahawkd.config.commandline.CommandlineConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;

@BelongsTo(CommandlineConfig.class)
public class MiscConfigDelegate extends AbstractConfigDelegate {

	@Parameter(names = "--debug", description = "Show debug output (sets logLevel to DEBUG)")
	private boolean debug = false;

	@Parameter(names = "--quiet", description = "No output (sets logLevel to NONE)")
	private boolean quiet = false;

	public boolean isDebug() {
		return debug;
	}

	public boolean isQuiet() {
		return quiet;
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
			return;
		}

		if (quiet) {
			Configurator.setAllLevels("io.tomahawkd.tlstester", Level.OFF);
		}
	}
}
