package io.tomahawkd.tlstester.testssl.data.parser;

import io.tomahawkd.tlstester.common.log.Logger;

import java.util.ArrayList;
import java.util.List;

public class CipherSuiteSet {

	private static final Logger logger = Logger.getLogger(CipherSuiteSet.class);

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
		logger.debug("Adding cipher suite" + suite);
		this.list.add(suite);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		list.forEach(e -> builder.append(e).append("\n"));
		return builder.toString();
	}
}
