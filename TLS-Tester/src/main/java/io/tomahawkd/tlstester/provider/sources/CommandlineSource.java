package io.tomahawkd.tlstester.provider.sources;

import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.provider.TargetStorage;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Objects;

@Source(name = InternalNamespaces.Sources.COMMANDLINE)
public class CommandlineSource extends AbstractTargetSource {

	public CommandlineSource(String args) {
		super(args);
	}

	@Override
	public void acquire(TargetStorage storage) {
		Arrays.stream(this.args.split(";"))
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
