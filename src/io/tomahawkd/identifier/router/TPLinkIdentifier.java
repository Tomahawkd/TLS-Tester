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
					String.valueOf(b.getPort()).contains("80") ||
					b.getPort() == 8888 ||
					b.getPort() == 81 || b.getPort() == 82 || b.getPort() == 83 || b.getPort() == 84) {

				Map<String, String> header = parseHttpHeader(b.getData());
				String result = header != null ? header.get("WWW-Authenticate") : "";
				return result != null && result.contains("TP-Link");
			}
		}

		return false;
	}

}
