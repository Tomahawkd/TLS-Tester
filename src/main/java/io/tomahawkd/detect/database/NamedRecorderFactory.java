package io.tomahawkd.detect.database;

import java.sql.SQLException;

public class NamedRecorderFactory extends AbstractRecorder {


	NamedRecorderFactory() throws SQLException {

	}

	public NamedRecorder get(String name) throws SQLException {
		return new NamedRecorder(name);
	}
}
