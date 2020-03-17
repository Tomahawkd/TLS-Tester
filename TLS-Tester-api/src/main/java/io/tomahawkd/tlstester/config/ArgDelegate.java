package io.tomahawkd.tlstester.config;

public interface ArgDelegate {

	void applyDelegate(ArgConfig config);

	void postParsing();

	<T> T getField(String key, Class<T> type);
}
