package io.tomahawkd.common.log;

import java.util.HashMap;
import java.util.Map;

// At this time, the class is to cache existing loggers.
public class LoggerManager {

	private Map<String, Logger> loggers;
	private static LogLevel defaultLevel = LogLevel.OK;
	private static LoggerManager manager = new LoggerManager();

	private LoggerManager() {
		this.loggers = new HashMap<>();
	}

	public static LoggerManager getInstance() {
		return manager;
	}

	Logger registerLogger(String name) {
		return loggers.computeIfAbsent(name, l -> new Logger(name));
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
