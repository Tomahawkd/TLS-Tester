package io.tomahawkd;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import io.tomahawkd.common.FileHelper;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.detect.database.Recorder;
import io.tomahawkd.detect.database.RecorderChain;
import io.tomahawkd.detect.database.RecorderManager;
import io.tomahawkd.detect.database.StatisticRecorder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public enum Config {

	INSTANCE;

	private Recorder recorder;
	private ConfigItems configItems;
	private static final Logger logger = Logger.getLogger(Config.class);

	Config() {
		configItems = new ConfigItems();
		initRecorders();
	}

	private void initRecorders() {
		try {

			List<String> activatedRecorders = configItems.getActivatedRecorder();

			if (activatedRecorders.size() == 0) recorder = new RecorderChain();
			else if (activatedRecorders.size() > 1) {
				RecorderChain chain = new RecorderChain();
				for (String recorderNames : configItems.getActivatedRecorder()) {

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

	public ConfigItems get() {
		return configItems;
	}

	public void loadFromFile(String file) throws IOException {
		configItems = new GsonBuilder().create().fromJson(FileHelper.readFile(file), ConfigItems.class);
	}

	public void saveConfig(String file) throws IOException {
		FileHelper.writeFile(file, new GsonBuilder().create().toJson(configItems), false);
	}

	public static class ConfigItems {

		@SerializedName("ignore_other_cert")
		private boolean otherSiteCert;
		@SerializedName("activated_recorder")
		private List<String> activatedRecorder;
		@SerializedName("thread_pool_timeout")
		private int executionPoolTimeout;
		@SerializedName("thread_count")
		private int threadCount;
		@SerializedName("testssl_path")
		private String testsslPath;

		private ConfigItems() {
			setDefault();
		}

		private void setDefault() {
			otherSiteCert = false;
			activatedRecorder = new ArrayList<>();
			executionPoolTimeout = 1;
			threadCount = 5;

			activatedRecorder.add("generic");
			activatedRecorder.add("statistic");
			testsslPath = "../testssl.sh";
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

		public String getTestsslPath() {
			return testsslPath;
		}
	}
}
