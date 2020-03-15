package io.tomahawkd.tlstester.data;

import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.analyzer.TreeCode;
import io.tomahawkd.tlstester.common.log.Logger;
import io.tomahawkd.tlstester.data.testssl.SegmentMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	@Nullable
	public Host getHostInfo() {
		return (Host) collectedData.get(InternalDataNamespace.SHODAN);
	}

	public String getCountryCode() {
		Host hostInfo = getHostInfo();
		if (hostInfo == null || hostInfo.getCountryCode() == null) return "null";
		else return hostInfo.getCountryCode();
	}

	@NotNull
	public SegmentMap getTargetData() {
		SegmentMap map = (SegmentMap) collectedData.get(InternalDataNamespace.TESTSSL);
		if (map == null) throw new RuntimeException("Required data not found");
		return map;
	}

	public boolean isHasSSL() {
		Boolean b = (Boolean) collectedData.get(InternalDataNamespace.HAS_SSL);
		return b == null ? false : b;
	}

	public String getBrand() {
		String brand = (String) collectedData.get(InternalDataNamespace.IDENTIFIER);
		if (brand == null) return "Unknown";
		return brand;
	}

	public String getCertHash() {
		try {
			return (String) getTargetData().get("cert_fingerprintSHA256").getResult();
		} catch (RuntimeException e) {
			return CERT_HASH_NULL;
		}
	}

	public void addResult(String name, TreeCode result) {
		analysisResult.put(name, result);
	}

	public Map<String, TreeCode> getAnalysisResult() {
		return analysisResult;
	}

	public static final String CERT_HASH_NULL = "null";
}
