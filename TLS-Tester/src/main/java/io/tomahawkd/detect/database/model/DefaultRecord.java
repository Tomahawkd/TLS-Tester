package io.tomahawkd.detect.database.model;

import io.tomahawkd.detect.LeakyChannelAnalyzer;
import io.tomahawkd.detect.PartiallyLeakyChannelAnalyzer;
import io.tomahawkd.detect.TaintedChannelAnalyzer;
import io.tomahawkd.detect.TreeCode;

public class DefaultRecord implements Record {

	private String ip;
	private int port;
	private String country;
	private boolean sslEnabled;
	private TreeCode leaky;
	private TreeCode tainted;
	private TreeCode partial;
	private String hash;

	private DefaultRecord(String ip, int port, String country, boolean sslEnabled,
	                      long leaky, long tainted, long partial, String hash) {
		this.ip = ip;
		this.port = port;
		this.country = country;
		this.sslEnabled = sslEnabled;
		this.leaky = new TreeCode(leaky, LeakyChannelAnalyzer.TREE_LENGTH);
		this.tainted = new TreeCode(tainted, TaintedChannelAnalyzer.TREE_LENGTH);
		this.partial = new TreeCode(partial, PartiallyLeakyChannelAnalyzer.TREE_LENGTH);
		this.hash = hash;
	}

	public DefaultRecord(String ip, int port, String country, boolean sslEnabled,
	                     TreeCode leaky, TreeCode tainted, TreeCode partial, String hash) {

		this(ip, port, country, sslEnabled,
				leaky == null ? 0 : leaky.getRaw(),
				tainted == null ? 0 : tainted.getRaw(),
				partial == null ? 0 : partial.getRaw(), hash);
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public String getCountry() {
		return country;
	}

	public boolean isSslEnabled() {
		return sslEnabled;
	}

	public TreeCode getLeaky() {
		return leaky;
	}

	public TreeCode getTainted() {
		return tainted;
	}

	public TreeCode getPartial() {
		return partial;
	}

	public String getHash() {
		return hash;
	}
}
