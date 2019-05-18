package io.tomahawkd;

import io.tomahawkd.common.log.Logger;
import io.tomahawkd.detect.database.NamedRecorderFactory;
import io.tomahawkd.detect.database.Recorder;
import io.tomahawkd.detect.database.RecorderManager;

import java.sql.SQLException;

public class Config {

	private static Recorder recorder;

	private static final Logger logger = Logger.getLogger(Config.class);

	static {
		try {
			recorder = RecorderManager.constructWithFactory(NamedRecorderFactory.class, "iot");

			if (recorder == null) throw new SQLException("Recorder is null, fallback to default");
		} catch (SQLException e) {
			logger.critical(e.getMessage());
		}
	}

	public static Recorder getRecorder() {
		return recorder;
	}
}
