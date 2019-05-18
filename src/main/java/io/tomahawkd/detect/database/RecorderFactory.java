package io.tomahawkd.detect.database;

import java.sql.SQLException;

public interface RecorderFactory {

	Recorder get(String name) throws SQLException;
}
