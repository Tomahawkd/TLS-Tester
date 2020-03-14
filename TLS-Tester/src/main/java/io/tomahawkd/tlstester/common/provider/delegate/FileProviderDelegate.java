package io.tomahawkd.tlstester.common.provider.delegate;

import io.tomahawkd.tlstester.common.provider.FileTargetProvider;
import io.tomahawkd.tlstester.common.provider.TargetProvider;

@SuppressWarnings("unused")
public class FileProviderDelegate implements ProviderDelegateParser {

	public static final String TYPE = "file";

	@Override
	public boolean identify(String type) {
		return TYPE.equalsIgnoreCase(type);
	}

	@Override
	public TargetProvider<String> parse(String v) {
		return FileTargetProvider.getDefault(v);
	}
}