package io.tomahawkd.tlstester.identifier;

import com.fooock.shodan.model.host.Host;

public interface Identifier {

	String tag();

	boolean identify(Host host);
}
