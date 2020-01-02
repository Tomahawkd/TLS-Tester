package io.tomahawkd.identifier;

import com.fooock.shodan.model.host.Host;

public abstract class CommonIdentifier {

	public abstract String tag();

	protected abstract boolean identify(Host host);

	protected boolean isWebPort(int port) {
		return String.valueOf(port).contains("443") ||
				String.valueOf(port).contains("80") ||
				port == 8888 || port == 81 || port == 82 || port == 83 || port == 84;
	}
}
