package io.tomahawkd.testssl;

import io.tomahawkd.detect.LeakyChannelAnalyzer;
import io.tomahawkd.testssl.data.SegmentMap;

public class Analyzer {

	public static void analyze(SegmentMap target) {
		if (LeakyChannelAnalyzer.checkVulnerable(target)) {
			System.out.println(target.getIp() + " is vulnerable.");
		} else {
			System.out.println(target.getIp() + " is not vulnerable.");
		}

//		if (TaintedChannelAnalyzer.checkVulnerable(target)) {
//			System.out.println(target.getIp() + " is vulnerable.");
//		} else {
//			System.out.println(target.getIp() + " is not vulnerable.");
//		}
	}
}
