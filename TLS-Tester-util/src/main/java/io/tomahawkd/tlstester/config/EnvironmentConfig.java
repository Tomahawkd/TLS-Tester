package io.tomahawkd.tlstester.config;

import io.tomahawkd.config.AbstractConfig;
import io.tomahawkd.config.annotation.SourceFrom;
import io.tomahawkd.config.sources.ConfigSource;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SourceFrom(EnvironmentConfigSource.class)
public class EnvironmentConfig extends AbstractConfig {

	private Map<String, String> env;

	@Override
	@SuppressWarnings("unchecked")
	public void parse(@NotNull ConfigSource source) {
		env = (Map<String, String>) source.getData();
	}

	public Map<String, String> getEnv() {
		return env;
	}
}
