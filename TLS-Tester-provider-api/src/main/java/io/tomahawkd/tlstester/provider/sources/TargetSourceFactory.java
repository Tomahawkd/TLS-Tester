package io.tomahawkd.tlstester.provider.sources;

public interface TargetSourceFactory {

	boolean identify(String type);

	TargetSource build(String args);
}
