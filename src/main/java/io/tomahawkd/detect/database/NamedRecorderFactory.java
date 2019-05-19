package io.tomahawkd.detect.database;

import java.sql.Connection;
import java.sql.SQLException;

public class NamedRecorderFactory implements RecorderFactory {

	private final Connection connection;

	NamedRecorderFactory(Connection connection) {
		this.connection = connection;
	}

	public NamedRecorder get(String name) throws SQLException {
		return new NamedRecorder(connection, name);
	}
}
