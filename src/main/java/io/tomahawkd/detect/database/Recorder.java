package io.tomahawkd.detect.database;

import io.tomahawkd.common.ThrowableBiConsumer;
import io.tomahawkd.detect.TreeCode;

import java.sql.Connection;

public interface Recorder {

	void addNonSSLRecord(String ip);

	void addRecord(String ip, boolean isSSL, TreeCode leaky, TreeCode tainted, TreeCode partial, String hash);

	void postUpdate(ThrowableBiConsumer<Connection, String> function) throws Exception;
}
