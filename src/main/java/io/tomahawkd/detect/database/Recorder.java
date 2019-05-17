package io.tomahawkd.detect.database;

public interface Recorder {

	void addNonSSLRecord(String ip);

	void addRecord(String ip, boolean isSSL, boolean leaky, boolean tainted, boolean partial, String hash);
}
