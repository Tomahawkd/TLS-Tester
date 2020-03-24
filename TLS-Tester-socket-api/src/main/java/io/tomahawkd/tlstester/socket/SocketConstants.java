package io.tomahawkd.tlstester.socket;

public class SocketConstants {

	// types
	public static final byte TYPE_DATA = 0x01;
	public static final byte TYPE_CTRL = 0x02;

	// status code
	public static final int OK = 0x1000;
	public static final int INVALID_CONTROL_BYTE = 0x1001;
	public static final int INSUFFICIENT_LENGTH = 0x1002;
	public static final int BAD_LENGTH = 0x1003;
	public static final int INVALID_VERSION = 0x1004;

	// control code
	public static final int CTRL_STOP = 0x0002;
}
