package io.tomahawkd.tlstester.extensions;

public interface ParameterizedExtensionHandler extends ExtensionHandler {
	/**
	 * Accept a extension instance
	 *
	 * @param extension extension
	 * @return true if accepted and false if rejected
	 */
	boolean accept(Class<? extends ParameterizedExtensionPoint> extension);
}
