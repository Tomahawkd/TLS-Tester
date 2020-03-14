package io.tomahawkd.tlstester.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.CommonIdentifier;

public class H3CIdentifier extends CommonIdentifier {
	@Override
	public String tag() {
		return "H3C";
	}

	@Override
	public boolean identify(Host host) {

		for (Banner banner : host.getBanners()) {

			switch (banner.getPort()) {
				case 23:
				case 161: return banner.getData().contains("H3C");
				default:
			}
		}

		return false;
	}
}
