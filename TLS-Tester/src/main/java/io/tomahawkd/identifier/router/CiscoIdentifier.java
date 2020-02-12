package io.tomahawkd.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.identifier.CommonIdentifier;

public class CiscoIdentifier extends CommonIdentifier {

	@Override
	public String tag() {
		return "Cisco";
	}

	@Override
	public boolean identify(Host host) {
		for (Banner banner : host.getBanners()) {

			switch (banner.getPort()) {
				case 23:
				case 2002: return banner.getData().contains("Cisco");
				case 80: return banner.getProduct().contains("Cisco");
				default:
			}
		}

		return false;
	}
}
