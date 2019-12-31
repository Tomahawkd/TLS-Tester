package io.tomahawkd;

import io.tomahawkd.common.log.Logger;
import io.tomahawkd.detect.database.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public enum Config {

	INSTANCE;

	private Recorder recorder;
	private Properties p;
	private ConfigDefaults configDefaults;
	private static final Logger logger = Logger.getLogger(Config.class);

	Config() {
		p = new Properties();
		configDefaults = new ConfigDefaults();
		initRecorders();
	}

	Config(String file) throws IOException {
		loadFromFile(file);
	}

	private void initRecorders() {
		try {

			List<String> activatedRecorders = configDefaults.getActivatedRecorder();

			if (activatedRecorders.size() == 0) recorder = new RecorderChain();
			else if (activatedRecorders.size() > 1) {
				RecorderChain chain = new RecorderChain();
				for (String recorderNames : configDefaults.getActivatedRecorder()) {

					Recorder r = RecorderManager.INSTANCE.getOrConstruct(recorderNames);
					if (recorderNames.equals("statistic")) {
						((StatisticRecorder) r).addTargetTables(activatedRecorders);
					}
					chain.addRecorder(r);
				}
				recorder = chain;
			} else {
				recorder = RecorderManager.INSTANCE.getOrConstruct(activatedRecorders.get(0));
			}
		} catch (SQLException e) {
			logger.critical(e.getMessage());
		}
	}

	public Recorder getRecorder() {
		return recorder;
	}

	public ConfigDefaults get() {
		return configDefaults;
	}

	private void loadFromFile(String file) throws IOException {
		p.load(new FileInputStream(new File(file)));
		configDefaults.overrideBy(p);
	}

	public void saveConfig(String file) throws IOException {
		configDefaults.overrideTo(p);
		p.store(new FileOutputStream(new File(file)), "");
	}

	public static class ConfigDefaults {

		private boolean otherSiteCert;
		private List<String> activatedRecorder;
		private int executionPoolTimeout;
		private int threadCount;

		private ConfigDefaults() {
			otherSiteCert = true;
			activatedRecorder = new ArrayList<>();
			executionPoolTimeout = 1;
			threadCount = 5;

			activatedRecorder.add("generic");
			activatedRecorder.add("statistic");
		}

		private void overrideBy(@NotNull Properties p) {
			otherSiteCert = Boolean.parseBoolean(p.getProperty("ignore_other_cert", "true"));

			String[] list = p.getProperty("activated_recorder", "").split(";");
			activatedRecorder.clear();
			activatedRecorder.addAll(Arrays.asList(list));

			executionPoolTimeout = Integer.parseInt(p.getProperty("thread_pool_timeout", "1"));
			threadCount = Integer.parseInt(p.getProperty("thread_count", "5"));
		}

		private void overrideTo(Properties p) {
			p.setProperty("ignore_other_cert", String.valueOf(otherSiteCert));

			StringBuilder builder = new StringBuilder();
			activatedRecorder.forEach(e -> builder.append(e).append(";"));
			p.setProperty("activated_recorder", builder.toString());

			p.setProperty("thread_pool_timeout", String.valueOf(executionPoolTimeout));
			p.setProperty("thread_count", String.valueOf(threadCount));
		}

		public boolean checkOtherSiteCert() {
			return otherSiteCert;
		}

		public List<String> getActivatedRecorder() {
			return activatedRecorder;
		}

		public int getExecutionPoolTimeout() {
			return executionPoolTimeout;
		}

		public int getThreadCount() {
			return threadCount;
		}
	}
}
