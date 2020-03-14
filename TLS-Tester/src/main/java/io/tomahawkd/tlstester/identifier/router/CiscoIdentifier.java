package io.tomahawkd.tlstester.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.CommonIdentifier;

public class CiscoIdentifier extends CommonIdentifier {

	@Override
	public String tag() {
		return "Cisco";
	}

	@Override
	public boolean identify(Host host) {
		for (Banner banner : host.getBanners()) {

			switch (banner.getPort()) {
				case 22:
				case 23:
				case 2002:
					return banner.getData().contains("Cisco") ||
							banner.getData().contains("C i s c o") ||
							banner.getData().contains("CISCO");
				case 80:
				case 443: return banner.getProduct().contains("Cisco");
				default:
			}
		}

		return false;
	}
}
