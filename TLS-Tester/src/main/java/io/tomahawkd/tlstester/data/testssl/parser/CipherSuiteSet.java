package io.tomahawkd.tlstester.data.testssl.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CipherSuiteSet {

	private static final Logger logger = LogManager.getLogger(CipherSuiteSet.class);

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
