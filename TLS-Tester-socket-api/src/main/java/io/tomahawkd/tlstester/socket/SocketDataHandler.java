package io.tomahawkd.tlstester.socket;

public interface SocketDataHandler {

	byte[] from(SocketData data);

	SocketData to(byte[] bytesData);
}
