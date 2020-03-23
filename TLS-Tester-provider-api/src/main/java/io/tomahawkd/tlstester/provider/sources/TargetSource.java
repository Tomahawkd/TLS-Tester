package io.tomahawkd.tlstester.provider.sources;

import io.tomahawkd.tlstester.provider.TargetStorage;

public interface TargetSource {

	void acquire(TargetStorage storage);
}
