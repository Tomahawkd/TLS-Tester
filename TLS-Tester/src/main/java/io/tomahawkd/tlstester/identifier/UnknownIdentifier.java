package io.tomahawkd.tlstester.identifier;

import com.fooock.shodan.model.host.Host;

public class UnknownIdentifier implements Identifier {

	public String tag() {
		return "Unknown";
	}

	public boolean identify(Host host) {
		return false;
	}
}
