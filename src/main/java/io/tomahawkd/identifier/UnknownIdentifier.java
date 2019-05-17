package io.tomahawkd.identifier;

import com.fooock.shodan.model.host.Host;

public class UnknownIdentifier extends CommonIdentifier {

	@Override
	public String tag() {
		return "Unknown";
	}

	@Override
	public boolean identify(Host host) {
		return true;
	}
}
