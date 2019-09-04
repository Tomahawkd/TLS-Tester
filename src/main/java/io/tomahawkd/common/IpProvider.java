package io.tomahawkd.common;

public interface IpProvider {

	boolean hasNextIp();

	String getNextIp();
}
