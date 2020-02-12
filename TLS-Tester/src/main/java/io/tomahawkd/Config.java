package io.tomahawkd;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import io.tomahawkd.common.FileHelper;
import io.tomahawkd.common.log.Logger;

import java.io.IOException;

public enum Config {

	INSTANCE;

	private final Logger logger = Logger.getLogger(Config.class);
	private ConfigItems configItems;

	Config() {
		initConfig();
	}

	private void initConfig() throws RuntimeException {

		logger.debug("Start load config.");

		String configPath = "./tlstester.config";
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
			!FileHelper.isFileExist(configItems.getTestsslPath() +
					"/openssl-iana.mapping.html")) {
			logger.fatal("Testssl components are missing.");
			throw new RuntimeException("Testssl components are missing");
		}

		logger.debug("Config load complete.");
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
		@SerializedName("thread_pool_timeout")
		private int executionPoolTimeout;
		@SerializedName("thread_count")
		private int threadCount;
		@SerializedName("testssl_path")
		private String testsslPath;
		@SerializedName("temp_expire_time")
		private int tempExpireTime;
		@SerializedName("db_name")
		private String dbName;

		private ConfigItems() {
			setDefault();
		}

		private void setDefault() {
			otherSiteCert = false;
			executionPoolTimeout = 1;
			threadCount = 5;
			tempExpireTime = 7;
			testsslPath = "../testssl.sh";
			dbName = "statistic.sqlite.db";
		}

		public boolean checkOtherSiteCert() {
			return otherSiteCert;
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

		public String getDbName() {
			return dbName;
		}
	}
}
