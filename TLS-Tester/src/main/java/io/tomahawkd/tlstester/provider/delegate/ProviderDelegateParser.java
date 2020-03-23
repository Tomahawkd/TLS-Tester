package io.tomahawkd.tlstester.provider.delegate;

import io.tomahawkd.tlstester.provider.TargetProvider;

public interface ProviderDelegateParser {
	boolean identify(String type);

	TargetProvider<String> parse(String v) throws Exception;
}
