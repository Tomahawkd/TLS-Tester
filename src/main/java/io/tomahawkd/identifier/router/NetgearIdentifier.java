package io.tomahawkd.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.identifier.CommonIdentifier;

import java.util.Map;

public class NetgearIdentifier extends CommonIdentifier {
	@Override
	public String tag() {
		return "Netgear";
	}

	@Override
	public boolean identify(Host host) {

		for (Banner banner : host.getBanners()) {

			if (isWebPort(banner.getPort()) || banner.getPort() == 7547 || banner.getPort() == 21) {

				return banner.getData().contains("NETGEAR") || banner.getData().contains("Netgear");
			}
		}

		return false;
	}
}
