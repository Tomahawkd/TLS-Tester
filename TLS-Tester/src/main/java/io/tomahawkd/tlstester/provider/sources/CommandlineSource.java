package io.tomahawkd.tlstester.provider.sources;

import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.provider.TargetStorage;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Objects;

@SuppressWarnings("unused")
@Source(name = InternalNamespaces.Sources.COMMANDLINE)
public class CommandlineSource extends AbstractTargetSource {

	public CommandlineSource(String args) {
		super(args);
	}

	@Override
	public void acquire(TargetStorage storage) {
		SourcesStreamHelper.addDataToStorage(storage, Arrays.stream(this.args.split(";")));
	}
}
