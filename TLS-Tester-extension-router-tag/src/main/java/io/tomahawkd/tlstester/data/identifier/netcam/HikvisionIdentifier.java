package io.tomahawkd.tlstester.data.identifier.netcam;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.identifier.CommonIdentifier;

public class HikvisionIdentifier extends CommonIdentifier {

	@Override
	public String tag() {
		return "Hikvision";
	}

	@Override
	public boolean identify(Host host) {
		for (Banner banner : host.getBanners()) {
			if(banner.getData().contains("Hikvision")) return true;
		}

		return false;
	}
}
