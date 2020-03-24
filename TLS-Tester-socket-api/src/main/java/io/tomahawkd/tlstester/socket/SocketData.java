package io.tomahawkd.tlstester.socket;

import java.util.ArrayList;
import java.util.List;

public class SocketData {

	private int status;
	private List<String> data = new ArrayList<>();
	private int optData;

	public SocketData(int status) {
		this(status, null, -1);
	}

	public SocketData(int status, int optData) {
		this(status, null, optData);
	}

	public SocketData(List<String> data) {
		this(SocketConstants.OK, data, -1);
	}

	public SocketData(int status, List<String> data, int optData) {
		this.status = status;
		if (data != null) this.data.addAll(data);
		this.optData = optData;
	}

	public int getStatus() {
		return status;
	}

	public List<String> getData() {
		return data;
	}

	public int getOptionalData() {
		return optData;
	}
}
