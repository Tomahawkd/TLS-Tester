package io.tomahawkd.tlstester.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.CommonIdentifier;

public class DrayTekIdentifier extends CommonIdentifier {

	@Override
	public String tag() {
		return "DrayTek";
	}

	@Override
	public boolean identify(Host host) {

		for (Banner banner : host.getBanners()) {

			switch (banner.getPort()) {
				case 161:
				case 1723: return banner.getData().contains("DrayTek");
				default:
			}
		}

		return false;
	}
}
