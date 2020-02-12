package io.tomahawkd.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.identifier.CommonIdentifier;

public class ZTEIdentifier extends CommonIdentifier {
	@Override
	public String tag() {
		return "ZTE";
	}

	@Override
	public boolean identify(Host host) {

		for (Banner banner : host.getBanners()) {

			if (banner.getPort() == 23) {

				return banner.getData().contains("ZTE");
			}
		}

		return false;
	}
}
