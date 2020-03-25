package io.tomahawkd.tlstester.socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class DataSocketDataHandler implements SocketDataHandler {

	private static final Logger logger = LogManager.getLogger(DataSocketDataHandler.class);

	// Data strcture is as follows:
	// +---------------------+-------------+-----------------+---------+
	// |  overall_length(4)  |           list(overall_length)          |
	// +---------------------+-------------+-----------------+---------+
	// |  overall_length(4)  |  version(1) | target_ip(16)   | port(2) |
	// +---------------------+-------------+----+------------+---------+
	// | The data overall    | ip version  | ip | ipv6 or    | target  |
	// | length              |             | v4 | blank      | port    |
	// |                     |             | 4B | 12B        |         |
	// +---------------------+-------------+-----------------+---------+
	// please note that the ipv6 is not support for TLS-Tester, though
	// we leave it here
	@Override
	public byte[] from(SocketData data) {
		List<InetSocketAddress> hostList = data.getData();

		List<byte[]> byteList = new ArrayList<>();
		for (InetSocketAddress host : hostList) {

			InetAddress add = host.getAddress();
			short port = (short) (host.getPort() & 0x0000ffff);
			byteList.add(
					ByteBuffer.allocate(19).order(ByteOrder.BIG_ENDIAN)
							.put((byte) 4)
							.put(add.getAddress())
							.putShort(17, port).array());
		}

		int overallLength = 19 * byteList.size();
		ByteBuffer buf =
				ByteBuffer.allocate(overallLength + 5).order(ByteOrder.BIG_ENDIAN)
						.put(SocketConstants.TYPE_DATA)
						.putInt(overallLength);

		for (byte[] bytes : byteList) {
			buf.put(bytes);
		}
		return buf.array();
	}

	@Override
	public SocketData to(byte[] bytesData) {

		logger.debug("Received data: {}", () -> {
			StringBuilder sb = new StringBuilder();
			for (byte b : bytesData)
				sb.append(String.format("%02x", b));
			return sb.toString();
		});

		ByteBuffer buffer =
				ByteBuffer.wrap(bytesData).order(ByteOrder.BIG_ENDIAN);

		int overallLength = buffer.getInt();
		if (bytesData.length != overallLength + 4) {
			return new SocketData(SocketConstants.INSUFFICIENT_LENGTH);
		}

		if (overallLength % 19 != 0) {
			return new SocketData(SocketConstants.BAD_LENGTH);
		}

		List<InetSocketAddress> targetList = new ArrayList<>();
		while (overallLength > 0) {
			int version = buffer.get();
			InetAddress add;

			try {
				// buffer.get will advance the position
				if (version == 4) {
					byte[] byteAdd = new byte[4];
					buffer.get(byteAdd);
					add = Inet4Address.getByAddress(byteAdd);

					// skip 12
					buffer.getLong();
					buffer.getInt();
				} else if (version == 6) {
					byte[] byteAdd = new byte[16];
					buffer.get(byteAdd);
					add = Inet6Address.getByAddress(byteAdd);
				} else {
					logger.warn("Invalid version {}, skipping", version);
					continue;
				}
			} catch (UnknownHostException e) {
				logger.warn("Invalid host, skipping");
				continue;
			}

			int port = buffer.getShort() & 0x0000ffff;

			String target = add.getHostAddress() + ":" + port;
			targetList.add(new InetSocketAddress(add, port));
			logger.debug("Wrapped host: {}", target);
			overallLength -= 19;
		}

		return new SocketData(targetList);
	}
}
