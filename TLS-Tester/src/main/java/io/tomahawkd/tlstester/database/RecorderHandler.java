package io.tomahawkd.tlstester.database;

import io.tomahawkd.tlstester.ArgParser;
import io.tomahawkd.tlstester.common.ComponentsLoader;
import io.tomahawkd.tlstester.common.log.Logger;
import io.tomahawkd.tlstester.database.delegate.RecorderDelegate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
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

		RecorderDelegate delegate = null;
		for (Class<?> clazz : ComponentsLoader.INSTANCE.loadClassesByAnnotation(Database.class)) {
			if (RecorderDelegate.class.isAssignableFrom(clazz)) {
				if (Modifier.isAbstract(clazz.getModifiers())) continue;
				logger.debug("Checking Class " + clazz.getName());
				if (clazz.getAnnotation(Database.class).name().equals(name)) {
					try {
						logger.debug("Invoking target database initialization procedure.");
						if (clazz.getAnnotation(Database.class).authenticateRequired()) {
							delegate = (RecorderDelegate)
									clazz.getConstructor(String.class, String.class).newInstance(
											ArgParser.INSTANCE.get().getDbUser(),
											ArgParser.INSTANCE.get().getDbPass()
									);
						} else {
							delegate = (RecorderDelegate) clazz.newInstance();
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

		if (delegate == null) {
			logger.fatal("Target database entity not found.");
			throw new IllegalArgumentException("Database type not found.");
		} else {
			delegate.setDbName(ArgParser.INSTANCE.get().getDbName());
			this.recorder = new Recorder(delegate);
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
