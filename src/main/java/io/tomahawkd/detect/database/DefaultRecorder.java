package io.tomahawkd.detect.database;

import io.tomahawkd.common.log.Logger;

public class DefaultRecorder implements Recorder {

	private static final Logger logger = Logger.getLogger(DefaultRecorder.class);

	@Override
	public void addNonSSLRecord(String ip) {
		addRecord(ip, false, false, false, false, "");
	}

	@Override
	public void addRecord(String ip, boolean isSSL, boolean leaky, boolean tainted, boolean partial, String hash) {
		logger.critical("Recorder is not available, fallback to default");
	}
}
