package io.tomahawkd.detect.database;

import io.tomahawkd.common.log.Logger;
import io.tomahawkd.detect.TreeCode;

public class DefaultRecorder implements Recorder {

	private static final Logger logger = Logger.getLogger(DefaultRecorder.class);

	@Override
	public void addNonSSLRecord(String ip) {
		addRecord(ip,
				false, new TreeCode(), new TreeCode(), new TreeCode(), "");
	}

	@Override
	public void addRecord(String ip, boolean isSSL, TreeCode leaky, TreeCode tainted, TreeCode partial, String hash) {
		logger.critical("Recorder is not available, fallback to default");
	}

	@Override
	public void postUpdate() {
	}

}
