package io.tomahawkd.tlstester.database;

import io.tomahawkd.tlstester.analyzer.Analyzer;
import io.tomahawkd.tlstester.analyzer.Record;
import io.tomahawkd.tlstester.data.TargetInfo;
import io.tomahawkd.tlstester.database.delegate.RecorderDelegate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractRecorder implements Recorder {

	private static final Logger logger = LogManager.getLogger(AbstractRecorder.class);

	private List<Record> cachedList;
	private final Connection connection;
	private final RecorderDelegate delegate;

	public AbstractRecorder(@NotNull RecorderDelegate delegate) {
		logger.debug("Using delegate " + delegate.getClass().getName());
		this.delegate = delegate;
		logger.debug("Initializing database");

		if (!delegate.getClass().isAnnotationPresent(Database.class)) {
			logger.fatal("Database type is not declared in delegate: " + delegate.getClass().getName());
			throw new RuntimeException("No type declared in annotation");
		}

		if (!delegate.getClass().isAnnotationPresent(TypeMap.class)) {
			logger.fatal("Database type mapping is not declared in delegate: " +
					delegate.getClass().getName());
			throw new RuntimeException("No type mapping declared in annotation");
		}

		logger.debug("Caching recordable result metadata.");
		cachedList = new ArrayList<>();
		for (Class<?> clazz : new Reflections().getTypesAnnotatedWith(Record.class)) {
			if (Analyzer.class.isAssignableFrom(clazz)) {
				logger.debug("Caching class " + clazz.getName());
				cachedList.add(clazz.getAnnotation(Record.class));
			}
		}
		cachedList = Collections.unmodifiableList(cachedList);

		try {
			String url = delegate.getUrl();
			logger.debug("Database connection url constructed: " + url);
			connection = DriverManager.getConnection(url,
					delegate.getUsername(), delegate.getPassword());
			init();
			delegate.preInit(connection);
		} catch (SQLException e) {
			logger.fatal("Database initialization failed.");
			logger.fatal(e.getMessage());
			throw new RuntimeException(e);
		}

		logger.debug("Database initialization complete.");
	}

	public abstract void init() throws SQLException;

	public abstract void record(TargetInfo info);

	public abstract void postRecord();

	protected final List<Record> getCachedList() {
		return cachedList;
	}

	protected final Connection getConnection() {
		return connection;
	}

	protected final RecorderDelegate getDelegate() {
		return delegate;
	}

	public final void close() {
		try {
			logger.debug("Closing connection.");
			connection.close();
		} catch (SQLException e) {
			logger.warn("Unable to close connection.");
		}
	}
}
