package io.tomahawkd.tlstester.config;

import org.jetbrains.annotations.NotNull;

public enum ArgConfigurator {

	INSTANCE;

	private ArgConfig config;

	public void setConfig(ArgConfig config) {
		this.config = config;
	}

	public ArgConfig getConfig() {
		return config;
	}

	public void parseArgs(String[] args) {
		config.parseArgs(args);
	}

	public <T extends ArgDelegate> T getByType(@NotNull Class<T> type) {
		return config.getByType(type);
	}

	public ArgDelegate getByString(String type) {
		return config.getByString(type);
	}
}
