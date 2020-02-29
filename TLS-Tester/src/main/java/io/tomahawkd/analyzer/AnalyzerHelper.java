package io.tomahawkd.analyzer;

import io.tomahawkd.common.TriFunction;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.data.SectionType;
import io.tomahawkd.testssl.data.Segment;
import io.tomahawkd.testssl.data.SegmentMap;
import io.tomahawkd.testssl.data.parser.CipherInfo;
import io.tomahawkd.testssl.data.parser.CipherSuite;
import io.tomahawkd.testssl.data.parser.OfferedResult;
import io.tomahawkd.tlsattacker.DrownTester;
import io.tomahawkd.tlsattacker.HeartBleedTester;

import java.util.List;

public class AnalyzerHelper {

	private static final Logger logger = Logger.getLogger(AnalyzerHelper.class);

	static boolean isVulnerableTo(SegmentMap target, String tag) {
		Segment segment = target.get(tag);
		if (segment == null) {
			logger.fatal("No vulnerability tag");
			throw new IllegalArgumentException("No vulnerability tag");
		}
		if (segment.getTag().getType() != SectionType.VULNERABILITIES) {
			logger.fatal("Not a vulnerability tag");
			throw new IllegalArgumentException("Not a vulnerability tag");
		}

		boolean result = ((OfferedResult) segment.getResult()).isResult();
		try {
			if (tag.equals(VulnerabilityTags.DROWN)) result |=
					new DrownTester().test(target.getIp());
			else if (tag.equals(VulnerabilityTags.HEARTBLEED)) result |=
					new HeartBleedTester().test(target.getIp());
		} catch (Exception e) {
			logger.warn("Further " + tag + " test failed, return original result");
		}
		return result;
	}

	static CipherInfo getHighestSupportedCipherSuite(SegmentMap target) {
		List<Segment> list = target.getByType(SectionType.CIPHER_ORDER);
		CipherInfo max = null;
		for (Segment seg : list) {
			CipherInfo info = (CipherInfo) seg.getResult();
			if (max == null) max = info;
			else if (info.compare(max) > 0) max = info;
		}

		if (max == null) {
			logger.fatal("No cipher got from segment");
			throw new IllegalArgumentException("No cipher got from segment");
		}

		return max;
	}

	static boolean downgradeIsPossibleToAVersionOf(SegmentMap target,
	                                               CipherInfo.SSLVersion version,
	                                               TriFunction<CipherInfo.SSLVersion,
			                                               CipherSuite, SegmentMap,
			                                               Boolean> factor) {
		boolean result = false;
		List<Segment> list = target.getByType(SectionType.CIPHER_ORDER);

		outer:
		for (Segment seg : list) {
			CipherInfo info = (CipherInfo) seg.getResult();
			if (info.getSslVersion().getLevel() >= CipherInfo.SSLVersion.TLS1.getLevel()) {
				for (CipherSuite suite : info.getCipher().getList()) {
					if (factor.apply(info.getSslVersion(), suite, target)) {
						result = true;
						break outer;
					}
				}
			}
		}

		return result;
	}
}
