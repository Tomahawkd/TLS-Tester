package io.tomahawkd;

import io.tomahawkd.common.log.Logger;
import io.tomahawkd.detect.database.*;

import java.sql.SQLException;

public class Config {

	private static Recorder recorder;

	private static final Logger logger = Logger.getLogger(Config.class);

	static {
		try {
			Recorder r = RecorderManager.constructWithFactory(NamedRecorderFactory.class, "iot");
			Recorder statistic = RecorderManager.get(StatisticRecorder.class);

			if (r == null) {
				recorder = statistic;
				throw new SQLException("Recorder is null, fallback to default");
			}

			recorder = new RecorderChain().addRecorder(r).addRecorder(statistic);

		} catch (SQLException e) {
			logger.critical(e.getMessage());
		}
	}

	public static Recorder getRecorder() {
		return recorder;
	}
}
