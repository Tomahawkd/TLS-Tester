package io.tomahawkd.detect.database;

import io.tomahawkd.detect.TreeCode;

import java.sql.Connection;
import java.sql.SQLException;

public class GenericRecorder extends NamedRecorder {

	GenericRecorder(Connection connection) throws SQLException {

		super(connection, "generic");
	}

	@Override
	public void addRecord(String ip, boolean isSSL, TreeCode leaky, TreeCode tainted, TreeCode partial, String hash) {
		super.addRecord(ip, isSSL, leaky, tainted, partial, hash);
	}
}
