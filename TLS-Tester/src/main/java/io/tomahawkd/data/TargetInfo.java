package io.tomahawkd.data;

import com.fooock.shodan.model.host.Host;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.tomahawkd.common.FileHelper;
import io.tomahawkd.netservice.ShodanQueriesHelper;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.identifier.HostObserver;
import io.tomahawkd.testssl.TestsslExecutor;
import io.tomahawkd.testssl.data.Segment;
import io.tomahawkd.testssl.data.SegmentMap;

import java.net.InetSocketAddress;
import java.util.List;

public class TargetInfo {

	private static final Logger logger = Logger.getLogger(TargetInfo.class);

	private InetSocketAddress ip;
	private Host hostInfo;
	private SegmentMap targetData;

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
			hostInfo = hostObserver.getResult().get(0);
			if (hostInfo == null) logger.warn("Host query is null.");
		} else {
			logger.warn("Host query timeout.");
		}

		/////
		// Part II: Use testssl for information
		/////
		logger.debug("Start test using testssl");
		String path = TestsslExecutor.runTest(getIp());
		logger.debug("Parsing file " + path);

		String result = FileHelper.readFile(path);
		List<Segment> r = new GsonBuilder().create()
				.fromJson(result, new TypeToken<List<Segment>>(){}.getType());
		r.forEach(targetData::add);
	}

	public String getIp() {
		return ip.getAddress().getHostAddress() + ":" + ip.getPort();
	}

	public Host getHostInfo() {
		return hostInfo;
	}

	public SegmentMap getTargetData() {
		return targetData;
	}
}
