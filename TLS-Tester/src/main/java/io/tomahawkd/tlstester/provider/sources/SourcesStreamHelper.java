package io.tomahawkd.tlstester.provider.sources;

import io.tomahawkd.tlstester.provider.TargetStorage;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.stream.Stream;

public class SourcesStreamHelper {

	public static void addDataToStorage(TargetStorage storage, Stream<String> dataStream) {
		dataStream.distinct()
				.map(s -> {
			String[] l = s.split(":");
			try {
				int port = Integer.parseInt(l[1]);
				if (port < 0 || port > 0xFFFF) {
					throw new NumberFormatException("Illegal port " + port);
				}
				return new InetSocketAddress(l[0], port);
			} catch (NumberFormatException e) {
				return null;
			}
		}).filter(Objects::nonNull).forEach(storage::add);
	}
}
