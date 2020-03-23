package io.tomahawkd.tlstester.provider.sources;

@SuppressWarnings("unused")
public class CommandlineSourceFactory implements TargetSourceFactory {

	public static final String TYPE = "ips";

	@Override
	public boolean identify(String type) {
		return TYPE.equalsIgnoreCase(type);
	}

	@Override
	public TargetSource build(String args) {
		return new CommandlineSource(args);
	}
}
