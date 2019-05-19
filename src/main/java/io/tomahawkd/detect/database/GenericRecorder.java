package io.tomahawkd.detect.database;

import io.tomahawkd.common.log.Logger;

import java.sql.*;

public class GenericRecorder extends NamedRecorder {

	GenericRecorder(Connection connection) throws SQLException {

		super(connection, "generic");
	}

	@Override
	public void addRecord(String ip, boolean isSSL, long leaky, long tainted, long partial, String hash) {
		super.addRecord(ip, isSSL, leaky, tainted, partial, hash);
	}
}
