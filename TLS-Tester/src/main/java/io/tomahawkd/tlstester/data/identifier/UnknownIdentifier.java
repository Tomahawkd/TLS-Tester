package io.tomahawkd.tlstester.data.identifier;

import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.Identifier;

public class UnknownIdentifier implements Identifier {

	private String type = "";

	public UnknownIdentifier() {
	}

	public UnknownIdentifier(String type) {
		this.type = "(" + type + ")";
	}

	public String tag() {
		return "Unknown" + type;
	}

	public boolean identify(Host host) {
		return false;
	}
}
