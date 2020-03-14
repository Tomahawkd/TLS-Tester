package io.tomahawkd.tlstester.common.log;

import org.jetbrains.annotations.Nullable;

public enum LogLevel {
	DEBUG(-1, "DEBUG", ConsoleColors.PURPLE),
	OK(0, "OK", ConsoleColors.BLUE),
	INFO(1, "INFO", ConsoleColors.BLACK),
	LOW(2, "LOW", ConsoleColors.YELLOW_BRIGHT),
	WARN(3, "WARN", ConsoleColors.YELLOW),
	CRITICAL(4, "CRITICAL", ConsoleColors.RED),
	FATAL(5, "FATAL", ConsoleColors.RED_BOLD);

	private int level;
	private String name;
	private String color;

	LogLevel(int level, String name, String color) {
		this.level = level;
		this.name = name;
		this.color = color;
	}

	public int getLevel() {
		return level;
	}

	public String getName() {
		return name;
	}

	public String getColor() {
		return color;
	}

	@Nullable
	public static LogLevel toLevel(int level) {
		switch (level) {
			case -1: return DEBUG;
			case 0: return OK;
			case 1: return INFO;
			case 2: return LOW;
			case 3: return WARN;
			case 4: return CRITICAL;
			case 5: return FATAL;
			default: return null;
		}
	}
}
