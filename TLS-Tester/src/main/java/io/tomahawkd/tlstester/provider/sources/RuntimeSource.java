package io.tomahawkd.tlstester.provider.sources;

import io.tomahawkd.tlstester.provider.TargetStorage;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class RuntimeSource implements TargetSource {

	private List<InetSocketAddress> list = new ArrayList<>();

	@Override
	public void acquire(TargetStorage storage) {
		storage.addAll(list);
	}

	public void addAll(Collection<String> data) {
		SourcesStreamHelper.addTo(list, data.stream());
	}
}
