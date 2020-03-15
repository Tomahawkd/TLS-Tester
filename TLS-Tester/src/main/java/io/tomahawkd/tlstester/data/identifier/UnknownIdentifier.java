package io.tomahawkd.tlstester.data.identifier;

import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.Identifier;

public class UnknownIdentifier implements Identifier {

	public String tag() {
		return "Unknown";
	}

	public boolean identify(Host host) {
		return false;
	}
}
