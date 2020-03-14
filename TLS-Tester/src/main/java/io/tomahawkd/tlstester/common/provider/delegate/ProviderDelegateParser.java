package io.tomahawkd.tlstester.common.provider.delegate;

import io.tomahawkd.tlstester.common.provider.TargetProvider;

public interface ProviderDelegateParser {
	boolean identify(String type);

	TargetProvider<String> parse(String v) throws Exception;
}
