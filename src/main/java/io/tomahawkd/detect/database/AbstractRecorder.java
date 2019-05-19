package io.tomahawkd.detect.database;

import io.tomahawkd.common.ThrowableBiConsumer;
import io.tomahawkd.common.ThrowableFunction;
import io.tomahawkd.detect.TreeCode;

import java.sql.Connection;
import java.sql.SQLException;

abstract class AbstractRecorder implements Recorder {

	protected final Connection connection;

	AbstractRecorder(Connection connection) throws SQLException {
		this.connection = connection;
	}


	@Override
	public void addNonSSLRecord(String ip) {
		addRecord(ip, false,
				new TreeCode(), new TreeCode(), new TreeCode(), "");
	}

	@Override
	public void addRecord(String ip, boolean isSSL, TreeCode leaky, TreeCode tainted, TreeCode partial, String hash) {

	}
}
