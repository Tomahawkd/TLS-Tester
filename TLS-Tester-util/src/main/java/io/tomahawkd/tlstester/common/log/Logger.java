package io.tomahawkd.tlstester.common.log;

import java.util.ArrayList;
import java.util.List;

public class Logger {

	private List<LogHandler> handlers;
	private String name;

	public static Logger getLogger(Class<?> name) {
		return LoggerManager.INSTANCE.registerLogger(name.getName());
	}

	public static Logger getGlobal() {
		return LoggerManager.INSTANCE.registerLogger(null);
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

	public void log(LogLevel level, Object message) {
		LoggingRecord record;
		if (message == null) record = new LoggingRecord(level, name, "");
		else record = new LoggingRecord(level, name, message.toString());
		handlers.forEach(h -> h.applyMessage(record));
	}

	public void debug(Object message) {
		log(LogLevel.DEBUG, message);
	}

	public void ok(Object message) {
		log(LogLevel.OK, message);
	}

	public void info(Object message) {
		log(LogLevel.INFO, message);
	}

	public void low(Object message) {
		log(LogLevel.LOW, message);
	}

	public void warn(Object message) {
		log(LogLevel.WARN, message);
	}

	public void critical(Object message) {
		log(LogLevel.CRITICAL, message);
	}

	public void fatal(Object message) {
		log(LogLevel.FATAL, message);
	}
}
