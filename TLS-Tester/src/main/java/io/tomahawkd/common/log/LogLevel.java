package io.tomahawkd.common.log;

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
}
