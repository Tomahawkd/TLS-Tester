package io.tomahawkd.detect.database;

public interface Recorder {

	void addNonSSLRecord(String ip);

	void addRecord(String ip, boolean isSSL, long leaky, long tainted, long partial, String hash);
}
