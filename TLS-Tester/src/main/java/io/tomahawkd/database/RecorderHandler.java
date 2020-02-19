package io.tomahawkd.database;

import io.tomahawkd.ArgParser;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;

public enum RecorderHandler {

	INSTANCE;

	private Recorder recorder;

	RecorderHandler() {
		String name = ArgParser.INSTANCE.get().getDbType();
		Reflections r = new Reflections();

		for (Class<?> clazz : r.getTypesAnnotatedWith(Database.class)) {
			if (Recorder.class.isAssignableFrom(clazz)) {
				if(clazz.getAnnotation(Database.class).name().equals(name)) {
					try {
						if (clazz.getAnnotation(Database.class).authenticateRequired()) {
							clazz.getConstructor(String.class, String.class).newInstance(
									ArgParser.INSTANCE.get().getDbName(),
									ArgParser.INSTANCE.get().getDbPass()
							);
						} else {
							recorder = (Recorder) clazz.newInstance();
						}
					} catch (InstantiationException | IllegalAccessException |
							NoSuchMethodException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		if (recorder == null) {
			throw new IllegalArgumentException("Database type not found.");
		}
	}

	public Recorder getRecorder() {
		return recorder;
	}
}
