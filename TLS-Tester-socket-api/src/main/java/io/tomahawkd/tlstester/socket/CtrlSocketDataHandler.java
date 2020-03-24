package io.tomahawkd.tlstester.socket;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CtrlSocketDataHandler implements SocketDataHandler {

	// Data strcture is as follows:
	// +--------------+
	// | ctrl_code(4) |
	// +--------------+
	// | control code |
	// +--------------+
	//
	@Override
	public byte[] from(SocketData data) {
		return ByteBuffer.allocate(5)
				.order(ByteOrder.BIG_ENDIAN)
				.put(SocketConstants.TYPE_CTRL)
				.putInt(data.getOptionalData())
				.array();
	}

	@Override
	public SocketData to(byte[] bytesData) {
		if (bytesData.length != 4) {
			return new SocketData(SocketConstants.BAD_LENGTH);
		}

		int code = ByteBuffer.wrap(bytesData).order(ByteOrder.BIG_ENDIAN).getInt();
		return new SocketData(SocketConstants.OK, code);
	}
}
