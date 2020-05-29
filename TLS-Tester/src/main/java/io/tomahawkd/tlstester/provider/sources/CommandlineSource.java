package io.tomahawkd.tlstester.provider.sources;

import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.provider.TargetStorage;

import java.util.Arrays;

@SuppressWarnings("unused")
@Source(name = InternalNamespaces.Sources.COMMANDLINE)
public class CommandlineSource extends AbstractTargetSource {

	public CommandlineSource(String args) {
		super(args);
	}

	@Override
	public void acquire(TargetStorage storage) {
		SourcesStreamHelper.processTo(storage, Arrays.stream(this.args.split(";")));
	}
}
