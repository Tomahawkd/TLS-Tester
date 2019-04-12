package io.tomahawkd.common.log;

import java.util.ArrayList;
import java.util.List;

public class Logger {

	private List<LogHandler> handlers;
	private String name;

	public static Logger getLogger(Class name) {
		return LoggerManager.getInstance().registerLogger(name.getName());
	}

	public static Logger getGlobal() {
		return LoggerManager.getInstance().registerLogger(null);
	}

	Logger(String name) {
		this.handlers = new ArrayList<>();
		this.name = name;
	}

	public void addHandler(LogHandler handler) {
		handlers.add(handler);
	}

	public void setLoggingLevel(LogLevel level) {
		handlers.forEach(handler -> handler.setLoggingLevel(level));
	}

	public void log(LogLevel level, String message) {
		LoggingRecord record = new LoggingRecord(level, name, message);

		// while no handler present, set default handler
		if (handlers.isEmpty()) handlers.add(new LogHandler());
		handlers.forEach(h -> h.applyMessage(record));
	}

	public void debug(String message) {
		log(LogLevel.DEBUG, message);
	}

	public void ok(String message) {
		log(LogLevel.OK, message);
	}

	public void info(String message) {
		log(LogLevel.INFO, message);
	}

	public void low(String message) {
		log(LogLevel.LOW, message);
	}

	public void warn(String message) {
		log(LogLevel.WARN, message);
	}

	public void critical(String message) {
		log(LogLevel.CRITICAL, message);
	}

	public void fatal(String message) {
		log(LogLevel.FATAL, message);
	}
}
