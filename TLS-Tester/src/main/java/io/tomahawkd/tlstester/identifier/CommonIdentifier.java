package io.tomahawkd.tlstester.identifier;

import com.fooock.shodan.model.host.Host;

public abstract class CommonIdentifier implements Identifier {

	public abstract String tag();

	public abstract boolean identify(Host host);

	protected boolean isWebPort(int port) {
		return String.valueOf(port).startsWith("443") ||
				String.valueOf(port).startsWith("80") ||
				port == 8888 || port == 81 || port == 82 || port == 83 || port == 84;
	}
}
