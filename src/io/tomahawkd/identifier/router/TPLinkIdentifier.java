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

		for (Banner b : host.getBanners()) {
			if (String.valueOf(b.getPort()).contains("443") ||
					String.valueOf(b.getPort()).contains("80")) {

				Map<String, String> header = parseHttpHeader(b.getData());
				String result = header != null ? header.get("WWW-Authenticate") : "";
				return result != null && result.contains("TP-Link");
			}
		}

		return false;
	}

}
