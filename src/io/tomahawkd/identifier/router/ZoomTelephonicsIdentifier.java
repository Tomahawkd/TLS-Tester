package io.tomahawkd.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.identifier.CommonIdentifier;

public class ZoomTelephonicsIdentifier extends CommonIdentifier {
	@Override
	public String tag() {
		return "Zoom Telephonics";
	}

	@Override
	public boolean identify(Host host) {

		for (Banner banner : host.getBanners()) {

			if (banner.getPort() == 161) {

				return banner.getData().contains("Zoom Telephonics");
			}
		}

		return false;
	}
}
