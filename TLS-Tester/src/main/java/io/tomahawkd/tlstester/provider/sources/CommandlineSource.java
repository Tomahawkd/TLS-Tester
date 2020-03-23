package io.tomahawkd.tlstester.provider.sources;

import io.tomahawkd.tlstester.provider.TargetStorage;

import java.util.Arrays;

public class CommandlineSource implements TargetSource {

	private String args;

	public CommandlineSource(String args) {
		this.args = args;
	}

	@Override
	public void acquire(TargetStorage storage) {
		storage.addAll(Arrays.asList(this.args.split(";")));
	}
}
