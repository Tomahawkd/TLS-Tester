package io.tomahawkd.tlstester.provider.sources;

import java.io.FileNotFoundException;

@SuppressWarnings("unused")
public class FileSourceFactory implements TargetSourceFactory {

	public static final String TYPE = "file";

	@Override
	public boolean identify(String type) {
		return TYPE.equalsIgnoreCase(type);
	}

	@Override
	public TargetSource build(String args) {
		try {
			return new FileSource(args);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
