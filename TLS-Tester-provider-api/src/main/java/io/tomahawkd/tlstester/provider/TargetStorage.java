package io.tomahawkd.tlstester.provider;

import java.util.Collection;

public interface TargetStorage {

	boolean hasMoreData();

	String getNextTarget();

	void add(String data);

	void addAll(Collection<String> data);
}
