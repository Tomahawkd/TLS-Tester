package io.tomahawkd.detect.database;

import io.tomahawkd.common.log.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RecorderManager {

	private static Map<Class<? extends AbstractRecorder>, Recorder> recorderMap = new HashMap<>();
	private static Map<Class<? extends RecorderFactory>, RecorderFactory> factoryMap = new HashMap<>();

	private static final DefaultRecorder defaultRecorder = new DefaultRecorder();

	private static final Logger logger = Logger.getLogger(RecorderManager.class);

	static {
		try {
			recorderMap.put(GenericRecorder.class, new GenericRecorder());
			recorderMap.put(StatisticRecoder.class, new StatisticRecoder());
			factoryMap.put(NamedRecorderFactory.class, new NamedRecorderFactory());

		} catch (SQLException e) {
			logger.critical("Database connect failed, fallback to default");
		}
	}

	@NotNull
	public static Recorder get(Class<? extends AbstractRecorder> clazz) {
		return recorderMap.getOrDefault(clazz, defaultRecorder);
	}

	@Nullable
	public static Recorder constructWithFactory(Class<? extends RecorderFactory> factory, String name)
			throws SQLException {
		return factoryMap.get(factory).get(name);
	}

	public static Recorder getDefault() {
		return defaultRecorder;
	}
}
