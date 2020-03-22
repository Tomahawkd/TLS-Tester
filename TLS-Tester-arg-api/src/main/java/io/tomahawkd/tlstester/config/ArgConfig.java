package io.tomahawkd.tlstester.config;

import org.jetbrains.annotations.NotNull;

/**
 * Arg config for configuring preference from commandline.
 */
public interface ArgConfig {

	/**
	 * Add new delegate for parsing additional commandline args
	 *
	 * @param delegate arg parsing delegate
	 */
	void addDelegate(ArgDelegate delegate);

	/**
	 * Get specific delegate by type.
	 *
	 * @param type Class of ArgDelegate
	 * @param <T> subclass of ArgDelegate
	 * @return delegate
	 */
	<T extends ArgDelegate> T getByType(@NotNull Class<T> type);

	/**
	 * Get specific delegate by type string.
	 * For those which cannot access its type class among different extensions.
	 * You may use {@link ArgDelegate#getField(String, Class)} for field data
	 * acquirement.
	 *
	 * @param type full name type string including package
	 * @return delegate
	 */
	ArgDelegate getByString(String type);

	/**
	 * Parse commandline args
	 *
	 * @param args args in main method
	 */
	void parseArgs(String[] args);
}
