package io.tomahawkd.common.log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggingRecord {

	private long time;
	private LogLevel level;
	private String className;
	private String message;

	LoggingRecord(LogLevel level, String className, String message) {
		this.level = level;
		this.message = message;
		this.className = className;
		this.time = System.currentTimeMillis();
	}

	public LogLevel getLevel() {
		return level;
	}

	public String getMessage() {
		return message;
	}

	public String getClassName() {
		return className;
	}

	public long getTime() {
		return time;
	}

	public String getParsedTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd MMMM yyyy");
		return dateFormat.format(new Date(time));
	}

	@Override
	public String toString() {
		return getParsedTime() + " [" + className + "] " + level.getName() + " " + message;
	}

	public String toConsoleString() {
		return level.getColor() + toString() + ConsoleColors.RESET;
	}
}
