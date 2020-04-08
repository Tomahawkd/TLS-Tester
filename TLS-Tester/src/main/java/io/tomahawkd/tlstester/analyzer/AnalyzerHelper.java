package io.tomahawkd.tlstester.analyzer;

import io.tomahawkd.tlstester.common.TriFunction;
import io.tomahawkd.tlstester.data.DataHelper;
import io.tomahawkd.tlstester.data.TargetInfo;
import io.tomahawkd.tlstester.data.testssl.SectionType;
import io.tomahawkd.tlstester.data.testssl.Segment;
import io.tomahawkd.tlstester.data.testssl.SegmentMap;
import io.tomahawkd.tlstester.data.testssl.parser.CipherInfo;
import io.tomahawkd.tlstester.data.testssl.parser.CipherSuite;
import io.tomahawkd.tlstester.data.testssl.parser.OfferedResult;
import io.tomahawkd.tlstester.tlsattacker.DrownTester;
import io.tomahawkd.tlstester.tlsattacker.HeartBleedTester;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class AnalyzerHelper {

	private static final Logger logger = LogManager.getLogger(AnalyzerHelper.class);

	static boolean isVulnerableTo(TargetInfo info, String tag) {
		SegmentMap target = DataHelper.getTargetData(info);
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
					new DrownTester().test(info);
			else if (tag.equals(VulnerabilityTags.HEARTBLEED)) result |=
					new HeartBleedTester().test(info);
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
