package io.tomahawkd.tlstester.provider.delegate;

import io.tomahawkd.tlstester.provider.FileTargetProvider;
import io.tomahawkd.tlstester.provider.TargetProvider;

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