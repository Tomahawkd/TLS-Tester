package io.tomahawkd.common.provider.delegate;

import io.tomahawkd.common.provider.TargetProvider;

public interface ProviderDelegateParser {
	boolean identify(String type);

	TargetProvider<String> parse(String v) throws Exception;
}
