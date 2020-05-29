package io.tomahawkd.tlstester.data;

import io.tomahawkd.tlstester.provider.TargetStorage;

@FunctionalInterface
public interface Callback {

	void call(TargetInfo info, TargetStorage storage);
}
