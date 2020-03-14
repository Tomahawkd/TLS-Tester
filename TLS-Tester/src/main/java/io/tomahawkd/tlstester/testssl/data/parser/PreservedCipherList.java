package io.tomahawkd.tlstester.testssl.data.parser;

import io.tomahawkd.tlstester.ArgParser;
import io.tomahawkd.tlstester.common.log.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeFilter;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class PreservedCipherList {

	private static final Logger logger = Logger.getLogger(PreservedCipherList.class);

	private static final Map<String, CipherSuite> map = new LinkedHashMap<>();
	private static final String path =
			ArgParser.INSTANCE.get().getTestsslPath() + "/openssl-iana.mapping.html";

	static {

		Document doc = null;
		try {

			logger.debug("Loading page " + path);
			doc = Jsoup.parse(new File(path), "utf-8");
			logger.debug("Page loaded");
		} catch (IOException e) {
			logger.critical("Error on loading page");
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
			String hexString = ele.child(0).text().trim();
			logger.debug("Hex[" + hexString + "] found");
			int hex = CommonParser.parseInt(hexString.substring(3, hexString.length() - 1), 16);

			String name = ele.child(1).text();
			logger.debug("Name[" + name + "] found");

			String keyExchange = ele.child(2).text();
			logger.debug("Key exchange[" + keyExchange + "] found");

			String encryption = ele.child(3).text();
			logger.debug("encryption[" + encryption + "] found");

			String bits = ele.child(4).text();
			if (bits.endsWith(", export")) bits = bits.split(",")[0];
			else if (bits.isEmpty()) bits = "-1";
			logger.debug("Bits[" + bits + "] found");

			String rfcName = "";
			try {
				rfcName = ele.child(5).text();
			} catch (IndexOutOfBoundsException ignore) {
			}
			if (name.isEmpty()) name = rfcName;
			logger.debug("RFC name[" + rfcName + "] found");


			CipherSuite cipher = new CipherSuite(hex, name, keyExchange, encryption, bits, rfcName);
			logger.debug("Parsed cipher suite " + cipher);

			map.put(name, cipher);
		}
	}

	public static CipherSuite getFromName(String name) {

		// Error on establish the map
		if (map.size() == 0) {
			logger.warn("List is not initialized");
			return null;
		} else return map.get(name);
	}
}
