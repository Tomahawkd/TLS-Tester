package io.tomahawkd.tlstester.database;

import io.tomahawkd.tlstester.config.ArgConfigurator;
import io.tomahawkd.tlstester.config.DatabaseArgDelegate;
import io.tomahawkd.tlstester.database.delegate.RecorderDelegate;
import io.tomahawkd.tlstester.extensions.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RecorderHandler implements ParameterizedExtensionHandler {

	private Class<? extends RecorderDelegate> delegateClass = null;

	private final Logger logger = LogManager.getLogger(RecorderHandler.class);
	private Recorder recorder;

	public RecorderHandler() {
	}

	@Override
	public boolean canAccepted(Class<? extends ExtensionPoint> clazz) {
		return RecorderDelegate.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean accept(ExtensionPoint extension) {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean accept(Class<? extends ParameterizedExtensionPoint> extension) {
		if (extension.getAnnotation(Database.class) == null) {
			logger.error("Recorder delegate {} do not have annotation Database, " +
							"rejected.",
					extension.getName());
			return false;
		}

		if (delegateClass != null) return true;

		String name =
				ArgConfigurator.INSTANCE.getByType(DatabaseArgDelegate.class).getDbType();

		if (name.equals(extension.getAnnotation(Database.class).name())) {
			logger.debug("Adding Recorder delegate {}", extension);
			delegateClass = (Class<? extends RecorderDelegate>) extension;
		}
		return true;
	}

	@Override
	public void postInitialization() {

		RecorderDelegate delegate = null;
		if (delegateClass != null) {
			try {
				if (delegateClass.getAnnotation(Database.class).authenticateRequired()) {
					delegate = delegateClass.getConstructor(String.class, String.class)
							.newInstance(
									ArgConfigurator.INSTANCE
											.getByType(DatabaseArgDelegate.class)
											.getDbUser(),
									ArgConfigurator.INSTANCE
											.getByType(DatabaseArgDelegate.class)
											.getDbPass()
							);
				} else {
					delegate = delegateClass.newInstance();
				}
			} catch (InstantiationException | IllegalAccessException |
					InvocationTargetException | NoSuchMethodException e) {
				logger.error("Unable to instantiate delegate {}",
						delegateClass.getName(), e);
			}
		}

		if (delegate == null) {
			logger.fatal("Target database entity not found.");
			throw new IllegalArgumentException("Database type not found.");
		}

		logger.debug("Using delegate {}", delegate.getClass());
		Database db = delegate.getClass().getAnnotation(Database.class);

		try {
			if (db.useDriver().isEmpty()) {

				logger.debug("Driver not declared, load all driver.");

				ExtensionManager.INSTANCE.loadClasses(Driver.class).stream()
						.filter(e -> !DriverDelegate.class.equals(e))
						.filter(e -> !Modifier.isAbstract(e.getModifiers()))
						.forEach(driver -> {
							logger.debug("Loading db driver " + driver.getName());
							try {
								DriverManager
										.registerDriver(
												new DriverDelegate(driver.newInstance()));
							} catch (SQLException |
									InstantiationException |
									IllegalAccessException e) {
								logger.error(
										"Unable to register driver {}",
										driver.getName(), e);
							}
						});
			} else {
				Class<?> c = ExtensionManager.INSTANCE.loadClass(db.useDriver());
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
		} catch (InstantiationException e) {
			logger.fatal(
					"Invoking database initialization procedure failed.", e);
			throw new RuntimeException(e);
		}

		delegate.setDbName(ArgConfigurator.INSTANCE.getByType(DatabaseArgDelegate.class)
				.getDbName());
		this.recorder = new Recorder(delegate);
	}

	public Recorder getRecorder() {
		if (recorder == null) postInitialization();
		return recorder;
	}

	public void close() {
		if (recorder == null) return;
		recorder.close();
		recorder = null;
	}
}
