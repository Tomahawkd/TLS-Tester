package io.tomahawkd.tlstester.common.log;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// At this time, the class is to cache existing loggers.
public enum LoggerManager {

	INSTANCE;

	private Map<String, Logger> loggers;
	private static LogLevel defaultLevel = LogLevel.OK;
	private LogFileOutputDelegate outputDelegate = new LogFileOutputDelegate();

	LoggerManager() {
		this.loggers = new HashMap<>();
	}

	Logger registerLogger(String name) {
		return loggers.computeIfAbsent(name, lname -> {
			Logger logger = new Logger(lname);

			LogHandler fileHandler = new LogHandler(defaultLevel);
			fileHandler.setFormatter(LoggingRecord::toString);
			fileHandler.setOutput(outputDelegate);
			logger.addHandler(fileHandler);

			LogHandler consoleHandler = new LogHandler(defaultLevel);
			consoleHandler.setFormatter(LoggingRecord::toConsoleString);
			consoleHandler.setOutput(System.out::println);
			logger.addHandler(consoleHandler);

			return logger;
		});
	}

	public static void setLoggingLevel(LogLevel level) {
		defaultLevel = Objects.requireNonNull(level, "Level value not found.");
		INSTANCE.loggers.values().forEach(logger -> logger.setLoggingLevel(level));
	}

	static LogLevel getDefaultLevel() {
		return defaultLevel;
	}

	public static class Validator implements IParameterValidator {
		@Override
		public void validate(String name, String value) throws ParameterException {
			try {
				int level = Integer.parseInt(value);
				LogLevel l = LogLevel.toLevel(level);
				if (l == null) throw new ParameterException("Invalid level");
				LoggerManager.setLoggingLevel(l);
			} catch (NumberFormatException e) {
				throw new ParameterException(e);
			}

		}
	}
}
