package io.tomahawkd.tlstester.provider.sources;

import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.provider.TargetStorage;

import java.util.Arrays;

@Source(name = InternalNamespaces.Sources.COMMANDLINE)
public class CommandlineSource extends AbstractTargetSource {

	public CommandlineSource(String args) {
		super(args);
	}

	@Override
	public void acquire(TargetStorage storage) {
		storage.addAll(Arrays.asList(this.args.split(";")));
	}
}
