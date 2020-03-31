package io.tomahawkd.tlstester.data.identifier.netcam;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.CommonIdentifier;

public class AxisIdentifier extends CommonIdentifier {

	@Override
	public String tag() {
		return "Axis";
	}

	@Override
	public boolean identify(Host host) {
		for (Banner banner : host.getBanners()) {
			if (banner.getPort() == 21) {
				return banner.getTitle().contains("Axis");
			}
		}

		return false;
	}
}
