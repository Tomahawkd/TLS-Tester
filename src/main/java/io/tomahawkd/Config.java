package io.tomahawkd;

import io.tomahawkd.common.log.Logger;
import io.tomahawkd.detect.database.*;

import java.io.*;
import java.sql.SQLException;
import java.util.Properties;

public enum Config {

	INSTANCE;

	private Recorder recorder;
	private Properties p;
	private ConfigDefaults configDefaults;
	private boolean initialized = false;
	private static final Logger logger = Logger.getLogger(Config.class);

	Config() {
		p = new Properties();
		configDefaults = new ConfigDefaults();
	}

	public Recorder getRecorder() {
		if (!initialized) {
			try {
				Recorder r = RecorderManager.constructWithFactory(NamedRecorderFactory.class, "iot");
				Recorder statistic = RecorderManager.get(StatisticRecorder.class);
				if (statistic instanceof StatisticRecorder) {
					((StatisticRecorder) statistic).addTargetTable("iot");
				}

				if (r == null) {
					recorder = statistic;
					throw new SQLException("Recorder is null, fallback to default");
				}

				recorder = new RecorderChain().addRecorder(r).addRecorder(statistic);

				initialized = true;
			} catch (SQLException e) {
				logger.critical(e.getMessage());
			}
		}
		return recorder;
	}

	public void loadFromFile(String file) throws IOException {
		p.load(new FileInputStream(new File(file)));
		configDefaults.overrideBy(p);
	}

	public void saveConfig(String file) throws IOException {
		configDefaults.overrideTo(p);
		p.store(new FileOutputStream(new File(file)), "");
	}
}
