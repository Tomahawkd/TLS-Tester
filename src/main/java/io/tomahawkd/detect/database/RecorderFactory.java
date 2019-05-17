package io.tomahawkd.detect.database;

import io.tomahawkd.common.log.Logger;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RecorderFactory {

	private static Map<Class<? extends AbstractRecorder>, Recorder> recorderMap = new HashMap<>();
	private static final DefaultRecorder defaultRecorder = new DefaultRecorder();

	private static final Logger logger = Logger.getLogger(RecorderFactory.class);

	static {
		try {
			recorderMap.put(GenericRecorder.class, new GenericRecorder());
			recorderMap.put(StatisticRecoder.class, new StatisticRecoder());
			recorderMap.put(NamedRecorderFactory.class, new NamedRecorderFactory());

		} catch (SQLException e) {
			logger.critical("Database connect failed, fallback to default");
		}
	}

	@NotNull
	public static Recorder get(Class<? extends AbstractRecorder> clazz) {
		return recorderMap.getOrDefault(clazz, defaultRecorder);
	}
}
