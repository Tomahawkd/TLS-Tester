package io.tomahawkd.tlstester.data.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.CommonIdentifier;

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
