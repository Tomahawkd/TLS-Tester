package io.tomahawkd.database;

import io.tomahawkd.ArgParser;
import io.tomahawkd.analyzer.Analyzer;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.data.TargetInfo;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractRecorder implements Recorder {

	private static final Logger logger = Logger.getLogger(AbstractRecorder.class);

	protected Connection connection;
	protected List<Record> cachedList;

	public AbstractRecorder() {
		this(null, null);
	}

	public AbstractRecorder(@Nullable String user, @Nullable String pass) {

		logger.debug("Initializing database");

		if (!this.getClass().isAnnotationPresent(Database.class)) {
			logger.fatal("Database type is not declared in this class: " + this.getClass().getName());
			throw new RuntimeException("No type declared in annotation");
		}

		Database d = this.getClass().getAnnotation(Database.class);
		String url;
		switch (d.type()) {
			case FILE:
				url = "jdbc:" + d.name() + ":" +
						ArgParser.INSTANCE.get().getDbName() + d.extension();
				break;
			case NETWORK:
				url = "jdbc:" + d.name() + "://" + d.host() + "/" +
						ArgParser.INSTANCE.get().getDbName() + d.extension();
				break;
			default:
				logger.fatal("Database type is not implemented.");
				throw new RuntimeException("Type not implemented");
		}
		try {
			logger.debug("Database connection url constructed: " + url);
			connection = DriverManager.getConnection(url, user, pass);
			init();
		} catch (SQLException e) {
			logger.fatal("Database initialization failed.");
			logger.fatal(e.getMessage());
			throw new RuntimeException(e);
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
		logger.debug("Database initialization complete.");
	}

	protected abstract void init() throws SQLException;

	@Override
	public abstract void record(TargetInfo info);
}
