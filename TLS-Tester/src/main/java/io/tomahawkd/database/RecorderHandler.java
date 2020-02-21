package io.tomahawkd.database;

import io.tomahawkd.ArgParser;
import io.tomahawkd.common.log.Logger;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;

public enum RecorderHandler {

	INSTANCE;

	private final Logger logger = Logger.getLogger(RecorderHandler.class);
	private Recorder recorder;

	RecorderHandler() {
		String name = ArgParser.INSTANCE.get().getDbType();
		logger.debug("Searching target database entity.");

		for (Class<?> clazz : new Reflections().getTypesAnnotatedWith(Database.class)) {
			if (Recorder.class.isAssignableFrom(clazz)) {
				if(clazz.getAnnotation(Database.class).name().equals(name)) {
					try {
						logger.debug("Invoking target database initialization procedure.");
						if (clazz.getAnnotation(Database.class).authenticateRequired()) {
							clazz.getConstructor(String.class, String.class).newInstance(
									ArgParser.INSTANCE.get().getDbUser(),
									ArgParser.INSTANCE.get().getDbPass()
							);
						} else {
							recorder = (Recorder) clazz.newInstance();
						}
					} catch (InstantiationException | IllegalAccessException |
							NoSuchMethodException | InvocationTargetException e) {
						logger.fatal("Invoking database initialization procedure failed.");
						logger.fatal(e.getMessage());
						throw new RuntimeException(e);
					}
				}
			}
		}

		if (recorder == null) {
			logger.fatal("Target database entity not found.");
			throw new IllegalArgumentException("Database type not found.");
		}
	}

	public Recorder getRecorder() {
		return recorder;
	}
}
