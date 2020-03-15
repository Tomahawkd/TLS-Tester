package io.tomahawkd.tlstester.data.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.CommonIdentifier;

public class MikroTikIdentifier extends CommonIdentifier {

	@Override
	public String tag() {
		return "MikroTik";
	}

	@Override
	public boolean identify(Host host) {

		for (Banner banner : host.getBanners()) {

			switch (banner.getPort()) {
				case 23:
				case 1723:
				case 2723:
				case 23023: return banner.getData().contains("MikroTik");
				case 2000:
				case 8080: return banner.getProduct().contains("MikroTik");
				default:
			}
		}

		return false;
	}
}
