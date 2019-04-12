package io.tomahawkd.common.log;

public enum LogLevel {
	DEBUG(-1, "DEBUG"),
	OK(0, "OK"),
	INFO(1, "INFO"),
	LOW(2, "LOW"),
	WARN(3, "WARN"),
	CRITICAL(4, "CRITICAL"),
	FATAL(5, "FATAL");


	private int level;
	private String name;

	LogLevel(int level, String name) {
		this.level = level;
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public String getName() {
		return name;
	}
}
