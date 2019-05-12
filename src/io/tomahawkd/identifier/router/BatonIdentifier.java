package io.tomahawkd.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.identifier.CommonIdentifier;

import java.util.Map;

public class BatonIdentifier extends CommonIdentifier {

	@Override
	public String tag() {
		return "iBall-Baton";
	}

	@Override
	public boolean identify(Host host) {

		for (Banner banner : host.getBanners()) {

			if (isWebPort(banner.getPort())) {

				Map<String, String> header = parseHttpHeader(banner.getData());
				String result = header != null ? header.get("WWW-Authenticate") : "";
				return result != null && (
						result.contains("iBall-Baton"));
			}
		}

		return false;
	}
}
