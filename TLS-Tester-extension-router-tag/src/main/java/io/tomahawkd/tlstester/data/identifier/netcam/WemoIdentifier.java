package io.tomahawkd.tlstester.data.identifier.netcam;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.CommonIdentifier;

public class WemoIdentifier extends CommonIdentifier {
	@Override
	public String tag() {
		return "WeMo";
	}

	@Override
	public boolean identify(Host host) {
		for (Banner banner : host.getBanners()) {
			if (banner.getData().contains("WeMo")) return true;
			else if (banner.getData().contains("wemo")) return true;
		}

		return false;
	}
}
