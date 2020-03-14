package io.tomahawkd.tlstester.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.CommonIdentifier;


public class TPLinkIdentifier extends CommonIdentifier {

	@Override
	public String tag() {
		return "TP-Link";
	}

	@Override
	public boolean identify(Host host) {

		for (Banner banner : host.getBanners()) {
			if (isWebPort(banner.getPort()) || banner.getPort() == 1234) {

				return banner.getData().contains("TP-Link") ||
						banner.getData().contains("TP-LINK") ||
						banner.getData().contains("TL-") ||
						banner.getTitle().contains("TL-") ||
						banner.getProduct().contains("TP-LINK");
			}
		}

		return false;
	}

}
