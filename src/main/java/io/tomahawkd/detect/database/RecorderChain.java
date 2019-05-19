package io.tomahawkd.detect.database;

import io.tomahawkd.detect.TreeCode;
import org.jetbrains.annotations.Contract;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecorderChain implements Recorder {

	private List<Recorder> recorders = new ArrayList<>();

	public RecorderChain() {

	}

	@Contract("_ -> this")
	public RecorderChain addRecorder(Recorder recorder) {
		recorders.add(recorder);
		return this;
	}

	@Contract("_ -> this")
	public RecorderChain addChain(RecorderChain chain) {
		recorders.addAll(chain.recorders);
		return this;
	}

	@Override
	public void addNonSSLRecord(String ip) {
		recorders.forEach(r -> r.addNonSSLRecord(ip));
	}

	@Override
	public void addRecord(String ip, boolean isSSL, TreeCode leaky, TreeCode tainted, TreeCode partial, String hash) {
		recorders.forEach(r -> r.addRecord(ip, isSSL, leaky, tainted, partial, hash));
	}

	@Override
	public void postUpdate() throws SQLException {
		for (Recorder recorder : recorders) {
			recorder.postUpdate();
		}
	}
}
