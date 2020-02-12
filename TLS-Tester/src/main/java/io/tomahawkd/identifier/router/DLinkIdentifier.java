package io.tomahawkd.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.identifier.CommonIdentifier;

public class DLinkIdentifier extends CommonIdentifier {

	@Override
	public String tag() {
		return "D-Link";
	}

	@Override
	public boolean identify(Host host) {
		for (Banner banner : host.getBanners()) {

			switch (banner.getPort()) {
				case 21: return banner.getProduct().contains("D-Link");
				case 23: return banner.getData().contains("Dlink");
				default:
			}
		}

		return false;
	}
}
