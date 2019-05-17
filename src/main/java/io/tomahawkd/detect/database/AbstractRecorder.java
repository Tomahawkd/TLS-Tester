package io.tomahawkd.detect.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class AbstractRecorder implements Recorder {


	private final String sqlitePath = "./statistic.sqlite.db";
	protected final Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);

	AbstractRecorder() throws SQLException {
	}


	@Override
	public void addNonSSLRecord(String ip) {
		addRecord(ip, false, false, false, false, "");
	}

	@Override
	public void addRecord(String ip, boolean isSSL, boolean leaky, boolean tainted, boolean partial, String hash) {

	}
}
