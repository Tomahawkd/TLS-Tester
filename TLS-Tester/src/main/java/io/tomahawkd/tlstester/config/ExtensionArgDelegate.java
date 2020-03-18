package io.tomahawkd.tlstester.config;

import com.beust.jcommander.Parameter;

public class ExtensionArgDelegate extends AbstractArgDelegate {

	@Parameter(names = "--safe", description = "Ignore all extensions.")
	private boolean safeMode = false;

	@Parameter(names = "--extension", description = "manually set extensions' directory")
	private String extensionPath = "extensions/";

	public boolean isSafeMode() {
		return safeMode;
	}

	public String getExtensionPath() {
		return extensionPath;
	}

	@Override
	public void postParsing() {
		if (!extensionPath.endsWith("/")) {
			extensionPath = extensionPath.concat("/");
		}
	}
}
