package io.tomahawkd.tlstester.socket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class SocketData {

	private int status;
	private List<InetSocketAddress> data = new ArrayList<>();
	private int optData;

	public SocketData(int status) {
		this(status, null, -1);
	}

	public SocketData(int status, int optData) {
		this(status, null, optData);
	}

	public SocketData(List<InetSocketAddress> data) {
		this(SocketConstants.OK, data, -1);
	}

	public SocketData(int status, List<InetSocketAddress> data, int optData) {
		this.status = status;
		if (data != null) this.data.addAll(data);
		this.optData = optData;
	}

	public int getStatus() {
		return status;
	}

	public List<InetSocketAddress> getData() {
		return data;
	}

	public int getOptionalData() {
		return optData;
	}
}
