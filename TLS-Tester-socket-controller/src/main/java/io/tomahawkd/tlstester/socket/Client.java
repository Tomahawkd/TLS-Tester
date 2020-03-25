package io.tomahawkd.tlstester.socket;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {

	public static void main(String[] args) throws Exception {
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress("127.0.0.1", 23333), 5000);
		OutputStream out = socket.getOutputStream();

		byte[] b = stop();

		StringBuilder sb = new StringBuilder();
		for (byte by : b) sb.append(String.format("%02x", by));
		System.out.println(sb.toString());
		System.out.println(b.length);

		out.write(b);
		out.flush();
		socket.close();
	}

	private static byte[] stop() {
		return new CtrlSocketDataHandler()
				.from(new SocketData(SocketConstants.OK, SocketConstants.CTRL_STOP));
	}

	private static byte[] data() throws Exception {
		List<InetSocketAddress> data = new ArrayList<>();
		data.add(new InetSocketAddress("127.0.0.1", 443));
		return new DataSocketDataHandler()
				.from(new SocketData(data));

	}
}
