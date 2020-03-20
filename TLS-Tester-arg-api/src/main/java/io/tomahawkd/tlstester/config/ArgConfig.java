package io.tomahawkd.tlstester.config;

import org.jetbrains.annotations.NotNull;

public interface ArgConfig {

	void addDelegate(ArgDelegate delegate);

	<T extends ArgDelegate> T getByType(@NotNull Class<T> type);

	ArgDelegate getByString(String type);

	void parseArgs(String[] args);
}
