package io.tomahawkd.tlstester.data.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.CommonIdentifier;

public class CloudValleyIdentifier extends CommonIdentifier {
	@Override
	public String tag() {
		return "Cloud Valley";
	}

	@Override
	public boolean identify(Host host) {

		for (Banner banner : host.getBanners()) {

			if (banner.getPort() == 161) {
				return banner.getData().contains("Cloud Valley");
			}
		}

		return false;
	}
}
