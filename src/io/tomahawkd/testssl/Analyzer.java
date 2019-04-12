package io.tomahawkd.testssl;

import io.tomahawkd.common.log.Logger;
import io.tomahawkd.detect.LeakyChannelAnalyzer;
import io.tomahawkd.testssl.data.SegmentMap;

public class Analyzer {

	private static final Logger logger = Logger.getLogger(Analyzer.class);

	public static void analyze(SegmentMap target) {
		if (LeakyChannelAnalyzer.checkVulnerable(target)) {
			logger.warn("Result: " + target.getIp() + " is vulnerable.");
		} else {
			logger.ok("Result: " + target.getIp() + " is not vulnerable.");
		}

//		if (TaintedChannelAnalyzer.checkVulnerable(target)) {
//			logger.warn("Result: " + target.getIp() + " is vulnerable.");
//		} else {
//			logger.ok("Result: " + target.getIp() + " is not vulnerable.");
//		}
//
//		if (PartiallyLeakyChannelAnalyzer.checkVulnerable(target)) {
//			logger.warn("Result: " + target.getIp() + " is vulnerable.");
//		} else {
//			logger.ok("Result: " + target.getIp() + " is not vulnerable.");
//		}
	}
}
