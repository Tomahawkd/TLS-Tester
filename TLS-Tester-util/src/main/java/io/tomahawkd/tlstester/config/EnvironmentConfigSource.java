package io.tomahawkd.tlstester.config;

import io.tomahawkd.config.sources.ConfigSource;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentConfigSource implements ConfigSource {
	@Override
	public Map<String, String> getData() {
		Map<String, String> env = new HashMap<>();

		// System OS
		String os = System.getProperty("os.name");
		String osStr = EnvironmentConstants.LINUX;
		if (os.contains(EnvironmentConstants.WINDOWS)) {
			if (os.equals(EnvironmentConstants.WINDOWS10)) {
				osStr = EnvironmentConstants.WINDOWS10;
			} else {
				osStr = EnvironmentConstants.WINDOWS;
			}
		} else if (os.equals(EnvironmentConstants.MACOS)) {
			osStr = EnvironmentConstants.MACOS;
		}
		env.put(EnvironmentNames.SYSTEM_OS, osStr);


		return env;
	}
}
