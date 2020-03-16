package io.tomahawkd.tlstester.data;

import io.tomahawkd.tlstester.analyzer.TreeCode;
import io.tomahawkd.tlstester.common.log.Logger;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class TargetInfo {

	private static final Logger logger = Logger.getLogger(TargetInfo.class);

	private InetSocketAddress ip;
	private Map<String, Object> collectedData;
	private Map<String, TreeCode> analysisResult;

	public TargetInfo(String ip) {
		if (!ip.contains(":")) this.ip = new InetSocketAddress(ip, 443);
		else {
			String[] t = ip.split(":");
			try {
				this.ip = new InetSocketAddress(t[0], Integer.parseInt(t[1]));
			} catch (NumberFormatException e) {
				logger.critical("Invalid ip " + ip);
				throw new IllegalArgumentException("Invalid ip " + ip);
			}
		}
		this.analysisResult = new HashMap<>();
		this.collectedData = new HashMap<>();
	}

	public Map<String, Object> getCollectedData() {
		return collectedData;
	}

	public String getIp() {
		return ip.getAddress().getHostAddress();
	}

	public String getHost() {
		return ip.getAddress().getHostAddress() + ":" + ip.getPort();
	}

	public void addResult(String name, TreeCode result) {
		analysisResult.put(name, result);
	}

	public Map<String, TreeCode> getAnalysisResult() {
		return analysisResult;
	}

	public static final String CERT_HASH_NULL = "null";
}
