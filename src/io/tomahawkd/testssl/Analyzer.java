package io.tomahawkd.testssl;

import io.tomahawkd.common.log.Logger;
import io.tomahawkd.detect.LeakyChannelAnalyzer;
import io.tomahawkd.detect.PartiallyLeakyChannelAnalyzer;
import io.tomahawkd.detect.TaintedChannelAnalyzer;
import io.tomahawkd.testssl.data.SegmentMap;

public class Analyzer {

	private static final Logger logger = Logger.getLogger(Analyzer.class);

	public static void analyze(SegmentMap target) {
		if (LeakyChannelAnalyzer.checkVulnerability(target))
			logger.warn("Result: " + target.getIp() + " is vulnerable.");
		else logger.ok("Result: " + target.getIp() + " is not vulnerable.");

		if (TaintedChannelAnalyzer.checkVulnerability(target))
			logger.warn("Result: " + target.getIp() + " is vulnerable.");
		else logger.ok("Result: " + target.getIp() + " is not vulnerable.");

		if (PartiallyLeakyChannelAnalyzer.checkVulnerability(target))
			logger.warn("Result: " + target.getIp() + " is vulnerable.");
		else logger.ok("Result: " + target.getIp() + " is not vulnerable.");
	}
}
