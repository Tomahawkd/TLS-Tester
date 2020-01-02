package io.tomahawkd.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.identifier.CommonIdentifier;

public class TeldatIdentifier extends CommonIdentifier {
	@Override
	public String tag() {
		return "Teldat";
	}

	@Override
	public boolean identify(Host host) {

		for (Banner banner : host.getBanners()) {

			if (isWebPort(banner.getPort())) {
				return banner.getData().contains("TELDAT");
			} else if (banner.getPort() == 21 || banner.getPort() == 23) {
				return banner.getData().contains("Teldat");
			}
		}

		return false;
	}
}
