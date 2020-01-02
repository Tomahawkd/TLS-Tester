package io.tomahawkd.common.log;

import java.util.HashMap;
import java.util.Map;

// At this time, the class is to cache existing loggers.
public enum LoggerManager {

	INSTANCE;

	private Map<String, Logger> loggers;
	private static LogLevel defaultLevel = LogLevel.OK;
	private LogFileOutputDelegate outputDelegate = new LogFileOutputDelegate();

	private LoggerManager() {
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
		defaultLevel = level;
		INSTANCE.loggers.values().forEach(logger -> {
			logger.setLoggingLevel(level);
		});
	}

	static LogLevel getDefaultLevel() {
		return defaultLevel;
	}
}
