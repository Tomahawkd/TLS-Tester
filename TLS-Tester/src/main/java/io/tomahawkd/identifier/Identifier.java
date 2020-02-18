package io.tomahawkd.identifier;

import com.fooock.shodan.model.host.Host;

public interface Identifier {

	String tag();

	boolean identify(Host host);
}
