package io.tomahawkd.common.log;

import java.util.HashMap;
import java.util.Map;

// At this time, the class is to cache existing loggers.
public class LoggerManager {

	private Map<String, Logger> loggers;
	private static LogLevel defaultLevel = LogLevel.OK;
	private static LoggerManager manager = new LoggerManager();
	private LogFileOutputDelegate outputDelegate = new LogFileOutputDelegate();

	private LoggerManager() {
		this.loggers = new HashMap<>();
	}

	public static LoggerManager getInstance() {
		return manager;
	}

	Logger registerLogger(String name) {
		return loggers.computeIfAbsent(name, lname -> {
			Logger logger = new Logger(lname);

			LogHandler fileHandler = new LogHandler(defaultLevel);
			fileHandler.setOutput(outputDelegate);
			logger.addHandler(fileHandler);

			LogHandler consoleHandler = new LogHandler(defaultLevel);
			logger.addHandler(consoleHandler);

			return logger;
		});
	}

	public static void setLoggingLevel(LogLevel level) {
		defaultLevel = level;
		manager.loggers.values().forEach(logger -> {
			logger.setLoggingLevel(level);
		});
	}

	static LogLevel getDefaultLevel() {
		return defaultLevel;
	}
}
