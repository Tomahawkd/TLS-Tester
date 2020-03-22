package io.tomahawkd.tlstester.config;

import org.jetbrains.annotations.NotNull;

/**
 * Entry for arg processing.
 */
public enum ArgConfigurator {

	INSTANCE;

	private ArgConfig config;

	/**
	 * Set arg config for parsing and config accessing.
	 * Generally this is done by the program in initialization.
	 * Invoking this method to redirect config may produce
	 * unintended behaviours.
	 *
	 * @param config config
	 */
	public void setConfig(ArgConfig config) {
		this.config = config;
	}

	/**
	 * Get arg config for parsing and config accessing.
	 *
	 * @return config
	 */
	public ArgConfig getConfig() {
		return config;
	}

	/**
	 * Delegate method to {@link ArgConfig#parseArgs(String[])}
	 *
	 * @param args args
	 */
	public void parseArgs(String[] args) {
		config.parseArgs(args);
	}

	/**
	 * Delegate method to {@link ArgConfig#getByType(Class)}
	 *
	 * @param type Class of ArgDelegate
	 * @param <T> subclass of ArgDelegate
	 * @return delegate
	 */
	public <T extends ArgDelegate> T getByType(@NotNull Class<T> type) {
		return config.getByType(type);
	}

	/**
	 * Delegate method to {@link ArgConfig#getByString(String)}
	 *
	 * @param type full name type string including package
	 * @return delegate
	 */
	public ArgDelegate getByString(String type) {
		return config.getByString(type);
	}
}
