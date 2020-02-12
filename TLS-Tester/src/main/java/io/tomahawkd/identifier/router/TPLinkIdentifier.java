package io.tomahawkd.identifier.router;

import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.Host;
import io.tomahawkd.identifier.CommonIdentifier;

import java.util.Map;


public class TPLinkIdentifier extends CommonIdentifier {

	@Override
	public String tag() {
		return "TP-Link";
	}

	@Override
	public boolean identify(Host host) {

		for (Banner banner : host.getBanners()) {
			if (isWebPort(banner.getPort())) {

				Map<String, String> header = parseHttpHeader(banner.getData());
				String result = header != null ? header.get("WWW-Authenticate") : "";
				return result != null && (
						result.contains("TP-Link") ||
						result.contains("TP-LINK") ||
						result.contains("TL-") ||
						banner.getTitle().contains("TL-"));
			}
		}

		return false;
	}

}
