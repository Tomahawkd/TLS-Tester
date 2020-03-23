package io.tomahawkd.tlstester.provider.sources;

import io.tomahawkd.tlstester.provider.TargetStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RuntimeSource implements TargetSource {

	private List<String> list = new ArrayList<>();

	@Override
	public void acquire(TargetStorage storage) {
		storage.addAll(list);
	}

	public void addAll(Collection<String> data) {
		list.addAll(data);
	}
}
