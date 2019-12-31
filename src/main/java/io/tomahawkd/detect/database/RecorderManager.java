package io.tomahawkd.detect.database;

import io.tomahawkd.common.log.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public enum RecorderManager {

	INSTANCE;

	private Map<String, Recorder> recorderMap = new HashMap<>();
	private Map<String, RecorderFactory> factoryMap = new HashMap<>();
	private DefaultRecorder defaultRecorder = null;

	private static final Logger logger = Logger.getLogger(RecorderManager.class);

	private void connect() {
		try {
			String sqlitePath = "./statistic.sqlite.db";
			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);

			recorderMap.put("generic", new GenericRecorder(connection));
			recorderMap.put("hardware", new HardwareRecoder(connection));
			recorderMap.put("statistic", new StatisticRecorder(connection));
			factoryMap.put("default", new NamedRecorderFactory(connection));

			defaultRecorder = null;
		} catch (SQLException e) {
			defaultRecorder = new DefaultRecorder();
			logger.critical("Database connect failed, fallback to default");
		}
	}

	RecorderManager() {
		connect();
	}

	@NotNull
	public Recorder getOrConstruct(String recorderName) throws SQLException {

		if (!factoryMap.containsKey("default")) logger.warn("Default recorder factory not found");
		return getOrConstruct(recorderName, "default");
	}

	@NotNull
	public Recorder getOrConstruct(String recorderName, String factory) throws SQLException {

		if (defaultRecorder != null) {
			connect();
			if (defaultRecorder != null) return defaultRecorder;
		}

		Recorder r = recorderMap.get(recorderName);
		if (r == null) {
			RecorderFactory f = factoryMap.get(factory);
			if (f == null) {
				logger.warn("No satisfied recorder or factory, returning default recorder");
				// if we successfully connect to the db, the default recorder would be null
				r = new DefaultRecorder();
			} else {
				r = f.get(recorderName);
			}
		}

		return r;
	}
}
