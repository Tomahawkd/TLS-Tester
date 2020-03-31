package io.tomahawkd.tlstester.data.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.CommonIdentifier;

public class BelkinIdentifier extends CommonIdentifier {

	@Override
	public String tag() {
		return "Belkin";
	}

	@Override
	public boolean identify(Host host) {
		for (Banner banner : host.getBanners()) {
			if (banner.getPort() == 10000) {
				return banner.getData().contains("Belkin");
			}
		}
		return false;
	}
}
