package io.tomahawkd.identifier;

import com.fooock.shodan.model.host.Host;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class CommonIdentifier {

	public abstract String tag();

	public abstract boolean identify(Host host);

	@Nullable
	protected Map<String, String> parseHttpHeader(String header) {

		if (!header.startsWith("HTTP/")) return null;

		Map<String, String> headerMap = new HashMap<>();

		for (String s : header.split("\r\n")) {
			if (s.trim().isEmpty()) continue;

			if (s.trim().startsWith("HTTP/")) headerMap.put("status", s.trim());
			else {
				try {
					String[] kv = s.split(":");
					headerMap.put(kv[0].trim(), kv[1].trim());
				} catch (IndexOutOfBoundsException ignored) {
				}
			}
		}
		return headerMap;
	}
}
