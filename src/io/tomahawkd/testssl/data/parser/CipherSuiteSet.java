package io.tomahawkd.testssl.data.parser;

import java.util.ArrayList;
import java.util.List;

public class CipherSuiteSet {

	private List<CipherSuite> list;

	public CipherSuiteSet() {
		this.list = new ArrayList<>();
	}

	public List<CipherSuite> getList() {
		return list;
	}

	public void addAll(CipherSuiteSet set) {
		this.list.addAll(set.list);
	}

	void add(CipherSuite suite) {
		this.list.add(suite);
	}

	@Override
	public String toString() {
		var builder = new StringBuilder();
		list.forEach(e -> builder.append(e).append("\n"));
		return builder.toString();
	}
}
