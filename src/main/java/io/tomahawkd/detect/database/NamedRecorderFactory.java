package io.tomahawkd.detect.database;

import java.sql.SQLException;

public class NamedRecorderFactory implements RecorderFactory {


	NamedRecorderFactory() {

	}

	public NamedRecorder get(String name) throws SQLException {
		return new NamedRecorder(name);
	}
}
