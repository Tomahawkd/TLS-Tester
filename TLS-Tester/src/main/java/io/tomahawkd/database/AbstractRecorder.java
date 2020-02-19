package io.tomahawkd.database;

import io.tomahawkd.ArgParser;
import io.tomahawkd.analyzer.Analyzer;
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

	protected Connection connection;
	protected List<Record> cachedList;

	public AbstractRecorder() {
		this(null, null);
	}

	public AbstractRecorder(@Nullable String user, @Nullable String pass) {

		if (!this.getClass().isAnnotationPresent(Database.class)) {
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
				throw new RuntimeException("Type not implemented");
		}
		try {
			connection = DriverManager.getConnection(url, user, pass);
			init();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		cachedList = new ArrayList<>();
		for (Class<?> clazz : new Reflections().getTypesAnnotatedWith(Record.class)) {
			if (Analyzer.class.isAssignableFrom(clazz)) {
				cachedList.add(clazz.getAnnotation(Record.class));
			}
		}
		cachedList = Collections.unmodifiableList(cachedList);
	}

	protected abstract void init() throws SQLException;

	@Override
	public abstract void record(TargetInfo info);
}
