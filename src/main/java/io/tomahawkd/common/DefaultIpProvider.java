package io.tomahawkd.common;

import java.util.Iterator;
import java.util.List;

public class DefaultIpProvider implements IpProvider {

	private List<String> ipList;
	private Iterator<String> iterator;

	public DefaultIpProvider(List<String> ipList) {
		this.ipList = ipList;
		this.iterator = ipList.iterator();
	}

	@Override
	public boolean hasNextIp() {
		return iterator.hasNext();
	}

	@Override
	public String getNextIp() {
		return iterator.next();
	}
}
