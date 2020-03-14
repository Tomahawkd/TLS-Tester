package io.tomahawkd.tlstester.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.CommonIdentifier;

public class BatonIdentifier extends CommonIdentifier {

	@Override
	public String tag() {
		return "iBall-Baton";
	}

	@Override
	public boolean identify(Host host) {

		for (Banner banner : host.getBanners()) {

			if (isWebPort(banner.getPort())) {

				return banner.getData().contains("iBall-Baton");
			}
		}

		return false;
	}
}
