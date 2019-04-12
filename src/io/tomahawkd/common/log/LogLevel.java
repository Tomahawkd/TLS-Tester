package io.tomahawkd.common.log;

public enum LogLevel {
	DEBUG(-1, "DEBUG"),
	OK(0, "OK"),
	LOW(1, "LOW"),
	WARN(2, "WARN"),
	FATAL(3, "FATAL");


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
