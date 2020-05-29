package io.tomahawkd.tlstester.provider.sources;

public abstract class AbstractTargetSource implements TargetSource {

	protected String args;

	public AbstractTargetSource(String args) {
		this.args = args;
	}
}
