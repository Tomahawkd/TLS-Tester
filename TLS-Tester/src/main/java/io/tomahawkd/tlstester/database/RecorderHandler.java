package io.tomahawkd.tlstester.database;

import io.tomahawkd.tlstester.common.ComponentsLoader;
import io.tomahawkd.tlstester.config.ArgConfigurator;
import io.tomahawkd.tlstester.config.DatabaseArgDelegate;
import io.tomahawkd.tlstester.database.delegate.RecorderDelegate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;

public enum RecorderHandler {

	INSTANCE;

	private final Logger logger = LogManager.getLogger(RecorderHandler.class);
	private Recorder recorder;

	public void init() {
		open();
	}

	private void open() {
		String name =
				ArgConfigurator.INSTANCE.getByType(DatabaseArgDelegate.class).getDbType();
		logger.debug("Searching target database entity.");

		RecorderDelegate delegate = null;
		for (Class<?> clazz : ComponentsLoader.INSTANCE.loadClassesByAnnotation(Database.class)) {
			if (RecorderDelegate.class.isAssignableFrom(clazz)) {
				if (Modifier.isAbstract(clazz.getModifiers())) continue;
				logger.debug("Checking Class " + clazz.getName());
				if (clazz.getAnnotation(Database.class).name().equals(name)) {
					try {
						logger.debug("Invoking target database initialization procedure.");
						Database db = clazz.getAnnotation(Database.class);

						// load driver
						if (db.useDriver().isEmpty()) {

							logger.debug("Driver not declared, load all driver.");
							Set<Class<? extends Driver>> drivers =
									ComponentsLoader.INSTANCE.loadClasses(Driver.class);
							for (Class<? extends Driver> driver : drivers) {
								// ignore delegate itself
								if (DriverDelegate.class.equals(driver)) continue;
								// explicit ignore com.mysql.jdbc.Driver
								if ("com.mysql.jdbc.Driver".equals(driver.getName())) continue;

								logger.debug("Loading db driver " + driver.getName());
								try {
									DriverManager
											.registerDriver(
													new DriverDelegate(driver.newInstance()));
								} catch (SQLException |
										InstantiationException |
										IllegalAccessException e) {
									logger.warn(
											"Unable to register driver {}",
											driver.getName(), e);
								}
							}
						} else {
							Class<?> c = ComponentsLoader.INSTANCE.loadClass(db.useDriver());
							if (c == null) {
								throw new InstantiationException(
										"No such class " + db.useDriver());
							}

							if (!Driver.class.isAssignableFrom(c)) {
								throw new InstantiationException(
										"Class " + db.useDriver() +
												" is not assignable to Driver class");
							}
							try {
								DriverManager
										.registerDriver(
												new DriverDelegate((Driver) c.newInstance()));
							} catch (SQLException |
									InstantiationException |
									IllegalAccessException e) {
								logger.warn(
										"Unable to register driver {}",
										c.getName(), e);
							}
						}

						// instantiate delegate
						if (db.authenticateRequired()) {
							delegate = (RecorderDelegate)
									clazz.getConstructor(String.class, String.class).newInstance(
											ArgConfigurator.INSTANCE
													.getByType(DatabaseArgDelegate.class)
													.getDbUser(),
											ArgConfigurator.INSTANCE
													.getByType(DatabaseArgDelegate.class)
													.getDbPass()
									);
						} else {
							delegate = (RecorderDelegate) clazz.newInstance();
						}
						break;
					} catch (InstantiationException | IllegalAccessException |
							NoSuchMethodException | InvocationTargetException e) {
						logger.fatal(
								"Invoking database initialization procedure failed.", e);
						throw new RuntimeException(e);
					}
				}
			}
		}

		if (delegate == null) {
			logger.fatal("Target database entity not found.");
			throw new IllegalArgumentException("Database type not found.");
		} else {
			delegate.setDbName(ArgConfigurator.INSTANCE.getByType(DatabaseArgDelegate.class)
					.getDbName());
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
