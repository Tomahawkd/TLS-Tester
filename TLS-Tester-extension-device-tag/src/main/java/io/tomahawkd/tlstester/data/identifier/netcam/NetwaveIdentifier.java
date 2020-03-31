package io.tomahawkd.tlstester.data.identifier.netcam;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.CommonIdentifier;

public class NetwaveIdentifier extends CommonIdentifier {

	@Override
	public String tag() {
		return "Netwave";
	}

	@Override
	public boolean identify(Host host) {
		for (Banner banner : host.getBanners()) {

			if (isWebPort(banner.getPort())) {
				return banner.getData().contains("Netwave");
			}
		}
		return false;
	}
}
