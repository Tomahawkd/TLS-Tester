package io.tomahawkd.tlstester.data.identifier.netcam;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.CommonIdentifier;

public class DahuaIdentifier extends CommonIdentifier {
	@Override
	public String tag() {
		return "Dahua";
	}

	@Override
	public boolean identify(Host host) {
		for (Banner banner : host.getBanners()) {
			if (banner.getPort() == 37777) {
				return banner.getTitle().contains("Dahua DVR");
			}
		}
		return false;
	}
}
