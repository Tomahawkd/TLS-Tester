package io.tomahawkd.tlstester.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.CommonIdentifier;

public class FiberHomeIdentifier extends CommonIdentifier {
	@Override
	public String tag() {
		return "FiberHome";
	}

	@Override
	public boolean identify(Host host) {

		for (Banner banner : host.getBanners()) {

			if (banner.getPort() == 23) {
				return banner.getData().contains("Fiber Home");
			}
		}

		return false;
	}
}
