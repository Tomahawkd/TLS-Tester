package io.tomahawkd;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ConfigDefaults {

	private boolean otherSiteCert;
	private List<String> activatedRecorder;
	private int executionPoolTimeout;

	ConfigDefaults() {
		otherSiteCert = true;
		activatedRecorder = new ArrayList<>();
		executionPoolTimeout = 1;

		activatedRecorder.add("generic");
	}

	void overrideBy(@NotNull Properties p) {
		otherSiteCert = Boolean.parseBoolean(p.getProperty("ignore_other_cert", "true"));

		String[] list = p.getProperty("activated_recorder", "").split(";");
		activatedRecorder.clear();
		activatedRecorder.addAll(Arrays.asList(list));

		executionPoolTimeout = Integer.parseInt(p.getProperty("thread_pool_timeout", "1"));
	}

	void overrideTo(Properties p) {
		p.setProperty("ignore_other_cert", String.valueOf(otherSiteCert));

		StringBuilder builder = new StringBuilder();
		activatedRecorder.forEach(e -> builder.append(e).append(";"));
		p.setProperty("activated_recorder", builder.toString());

		p.setProperty("thread_pool_timeout", String.valueOf(executionPoolTimeout));
	}
}
