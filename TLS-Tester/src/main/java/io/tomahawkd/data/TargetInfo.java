package io.tomahawkd.data;

import com.fooock.shodan.model.host.Host;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.tomahawkd.analyzer.TreeCode;
import io.tomahawkd.common.FileHelper;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.exception.NoSSLConnectionException;
import io.tomahawkd.identifier.IdentifierHelper;
import io.tomahawkd.netservice.ShodanQueriesHelper;
import io.tomahawkd.testssl.TestsslExecutor;
import io.tomahawkd.testssl.data.Segment;
import io.tomahawkd.testssl.data.SegmentMap;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TargetInfo {

	private static final Logger logger = Logger.getLogger(TargetInfo.class);

	private InetSocketAddress ip;
	private Host hostInfo;
	private SegmentMap targetData;
	private String brand;
	private boolean hasSSL;
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
		this.targetData = new SegmentMap();
		this.analysisResult = new HashMap<>();
	}

	public void collectInfo() throws Exception {

		//////
		// Part I: Query Shodan for information
		//////
		logger.debug("Start query Shodan for ip information");
		HostObserver<Host> hostObserver = new HostObserver<>();
		ShodanQueriesHelper.searchWithIp(ip.getAddress().getHostAddress(), hostObserver);

		int timePassed = 0;
		while (!hostObserver.isComplete()) {
			try {
				if (timePassed > 60) {
					logger.warn("Target " + ip + " look up in Shodan failed.");
					break;
				}
				Thread.sleep(1000);
				timePassed += 1;
			} catch (InterruptedException e) {
				break;
			}
		}

		if (hostObserver.isComplete()) {
			List<Host> result = hostObserver.getResult();
			if (result.isEmpty()) logger.warn("Host query is null.");
			else hostInfo = hostObserver.getResult().get(0);
		} else {
			logger.warn("Host query timeout.");
		}

		/////
		// Part II: Identify brand
		/////
		brand = IdentifierHelper.identifyHardware(hostInfo).tag();

		/////
		// Part III: Use testssl for information
		/////
		try {
			logger.debug("Start test using testssl");
			String path = TestsslExecutor.runTest(getIp());
			logger.debug("Parsing file " + path);

			String result = FileHelper.readFile(path);
			List<Segment> r = new GsonBuilder().create()
					.fromJson(result, new TypeToken<List<Segment>>() {
					}.getType());
			r.forEach(targetData::add);
			hasSSL = true;
		} catch (NoSSLConnectionException e) {
			hasSSL = false;
		}
	}

	public String getIp() {
		return ip.getAddress().getHostAddress() + ":" + ip.getPort();
	}

	public Host getHostInfo() {
		return hostInfo;
	}

	public String getCountryCode() {
		if (hostInfo == null || hostInfo.getCountryCode() == null) return "null";
		else return hostInfo.getCountryCode();
	}

	public SegmentMap getTargetData() {
		return targetData;
	}

	public boolean isHasSSL() {
		return hasSSL;
	}

	public String getBrand() {
		return brand;
	}

	public String getCertHash() {
		try {
			return (String) targetData.get("cert_fingerprintSHA256").getResult();
		} catch (NullPointerException e) {
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
