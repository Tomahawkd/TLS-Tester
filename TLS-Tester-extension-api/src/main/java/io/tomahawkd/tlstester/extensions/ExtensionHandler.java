package io.tomahawkd.tlstester.extensions;

import java.util.List;

public interface ExtensionHandler {

	/**
	 * Accept a extension instance
	 *
	 * @param extension extension
	 * @return true if accepted and false if rejected
	 */
	boolean accept(ExtensionPoint extension);

	boolean canAccepted(Class<? extends ExtensionPoint> clazz);

	/**
	 * Post-Initialization procedure
	 */
	default void postInitialization() {}
}
