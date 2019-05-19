package io.tomahawkd.detect.database;

import io.tomahawkd.detect.TreeCode;

public interface Recorder {

	void addNonSSLRecord(String ip);

	void addRecord(String ip, boolean isSSL, TreeCode leaky, TreeCode tainted, TreeCode partial, String hash);
}
