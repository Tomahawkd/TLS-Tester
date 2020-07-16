package io.tomahawkd.tlstester.database;

import io.tomahawkd.tlstester.data.TargetInfo;

import java.sql.SQLException;

public interface Recorder {

	void init() throws SQLException;

	void record(TargetInfo info);

	void postRecord();

	void close();
}
