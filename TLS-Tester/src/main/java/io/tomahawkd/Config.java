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
	private final Logger logger = Logger.getLogger(Config.class);
	private final String configPath = "./tlstester.config";

	Config() {
		initConfig();
		initRecorders();
	}

	private void initConfig() throws RuntimeException {

		logger.debug("Start load config.");

		if (!FileHelper.isFileExist(configPath)) {
			logger.info("Config file not found, creating new.");
			configItems = new ConfigItems();
			try {
				saveConfig(configPath);
			} catch (IOException e) {
				logger.critical("Config file save failed.");
			}
		} else {
			logger.debug("Reading config file from " + configPath);
			try {
				loadFromFile(configPath);
			} catch (IOException e) {
				logger.fatal("Config file read failed.");
				throw new RuntimeException("Config file read failed");
			}
		}

		// do some checks
		if (!FileHelper.isFileExist(configItems.getTestsslPath() + "/testssl.sh") ||
			!FileHelper.isFileExist(configItems.getTestsslPath() + "/openssl-iana.mapping.html")) {
			logger.fatal("Testssl components are missing.");
			throw new RuntimeException("Testssl components are missing");
		}

		logger.debug("Config load complete.");
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
		configItems = new GsonBuilder().create()
				.fromJson(FileHelper.readFile(file), ConfigItems.class);
	}

	public void saveConfig(String file) throws IOException {
		FileHelper.writeFile(file,
				new GsonBuilder().setPrettyPrinting().create().toJson(configItems),
				false);
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
		@SerializedName("temp_expire_time")
		private int tempExpireTime;

		private ConfigItems() {
			setDefault();
		}

		private void setDefault() {
			otherSiteCert = false;
			activatedRecorder = new ArrayList<>();
			executionPoolTimeout = 1;
			threadCount = 5;
			tempExpireTime = 7;

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

		public int getTempExpireTime() {
			return tempExpireTime;
		}
	}
}
