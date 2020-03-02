package io.tomahawkd.database;

import io.tomahawkd.ArgParser;
import io.tomahawkd.common.ComponentsLoader;
import io.tomahawkd.common.log.Logger;

import java.lang.reflect.InvocationTargetException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;

public enum RecorderHandler {

	INSTANCE;

	private final Logger logger = Logger.getLogger(RecorderHandler.class);
	private Recorder recorder;

	public void init() {
		open();
	}

	private void open() {
		String name = ArgParser.INSTANCE.get().getDbType();
		logger.debug("Searching target database entity.");

		Set<Class<? extends Driver>> drivers = ComponentsLoader.INSTANCE.loadClasses(Driver.class);
		for (Class<? extends Driver> driver : drivers) {
			// ignore delegate itself
			if (DriverDelegate.class.equals(driver)) continue;

			logger.debug("Loading db driver " + driver.getName());
			try {
				DriverManager.registerDriver(new DriverDelegate(driver.newInstance()));
			} catch (SQLException | InstantiationException | IllegalAccessException e) {
				logger.warn("Unable to register driver " + driver.getName());
				logger.warn(e.getMessage());
			}
		}

		for (Class<?> clazz : ComponentsLoader.INSTANCE.loadClassesByAnnotation(Database.class)) {
			if (Recorder.class.isAssignableFrom(clazz)) {
				logger.debug("Checking Class " + clazz.getName());
				if (clazz.getAnnotation(Database.class).name().equals(name)) {
					try {
						logger.debug("Invoking target database initialization procedure.");
						if (clazz.getAnnotation(Database.class).authenticateRequired()) {
							recorder = (Recorder)
									clazz.getConstructor(String.class, String.class).newInstance(
											ArgParser.INSTANCE.get().getDbUser(),
											ArgParser.INSTANCE.get().getDbPass()
									);
						} else {
							recorder = (Recorder) clazz.newInstance();
						}
						break;
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
		if (recorder == null) open();
		return recorder;
	}

	public void close() {
		if (recorder == null) return;
		recorder.close();
		recorder = null;
	}
}
