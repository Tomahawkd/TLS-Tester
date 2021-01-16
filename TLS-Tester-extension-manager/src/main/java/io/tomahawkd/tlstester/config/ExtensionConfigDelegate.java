package io.tomahawkd.tlstester.config;

import com.beust.jcommander.Parameter;
import io.tomahawkd.config.AbstractConfigDelegate;
import io.tomahawkd.config.annotation.BelongsTo;
import io.tomahawkd.config.commandline.CommandlineConfig;

@BelongsTo(CommandlineConfig.class)
public class ExtensionConfigDelegate extends AbstractConfigDelegate {

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
