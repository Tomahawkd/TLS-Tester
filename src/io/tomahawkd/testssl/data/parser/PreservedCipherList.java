package io.tomahawkd.testssl.data.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeFilter;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class PreservedCipherList {

	public static final String TAG = "[PreservedCipherList]";

	private static final Map<String, CipherSuite> map = new LinkedHashMap<>();
	private static final String mappingUrl = "https://testssl.sh/openssl-iana.mapping.html";

	static {

		Document doc;
		try {
			doc = Jsoup.parse(new URL(mappingUrl), 60000);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(TAG + " Error on loading the target page.");
		}

		assert doc != null;

		Elements eles = doc.body().getElementsByTag("tbody");
		eles.filter(new NodeFilter() {
			@Override
			public FilterResult head(Node node, int depth) {
				if (node instanceof Element) return FilterResult.CONTINUE;
				else if (depth == 1) return FilterResult.REMOVE;
				else return FilterResult.CONTINUE;
			}

			@Override
			public FilterResult tail(Node node, int depth) {
				return FilterResult.CONTINUE;
			}
		});

		for (Element ele : eles.get(0).children()) {
			// Cipher Suite info
			var hexString = ele.child(0).text().trim();
			var hex = CommonParser.parseInt(hexString.substring(3, hexString.length() - 1), 16);
			var name = ele.child(1).text();
			var keyExchange = ele.child(2).text();
			var encryption = ele.child(3).text();
			var bits = ele.child(4).text();
			if (bits.endsWith(", export")) bits = bits.split(",")[0];
			else if (bits.isEmpty()) bits = "-1";
			var rfcName = "";
			try {
				rfcName = ele.child(5).text();
			} catch (IndexOutOfBoundsException ignore) {
			}
			if (name.isEmpty()) name = rfcName;

			var cipher = new CipherSuite(hex, name, keyExchange, encryption, bits, rfcName);
			map.put(name, cipher);
		}
	}

	public static CipherSuite getFromName(String name) {

		// Error on establish the map
		if (map.size() == 0) return null;
		else return map.get(name);
	}
}
