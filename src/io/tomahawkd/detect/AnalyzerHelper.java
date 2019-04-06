package io.tomahawkd.detect;

import io.tomahawkd.common.ShodanQueriesHelper;
import io.tomahawkd.testssl.Analyzer;
import io.tomahawkd.testssl.ExecutionHelper;
import io.tomahawkd.testssl.data.SectionType;
import io.tomahawkd.testssl.data.Segment;
import io.tomahawkd.testssl.data.SegmentMap;
import io.tomahawkd.testssl.data.TargetSegmentMap;
import io.tomahawkd.testssl.data.parser.OfferedResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

class AnalyzerHelper {

	public static final String TAG = "[AnalyzerHelper]";

	static boolean isVulnerableTo(SegmentMap target, String tag) {
		Segment segment = target.get(tag);
		if (segment == null) throw new IllegalArgumentException("No vulnerability tag");
		if (segment.getTag().getType() != SectionType.VULNERABILITIES)
			throw new IllegalArgumentException(TAG + " Not a vulnerability tag");
		return ((OfferedResult) segment.getResult()).isResult();
	}

	static boolean isOtherWhoUseSameCertVulnerableTo(SegmentMap target,
	                                                 Function<SegmentMap, Boolean> detect) {
		return isOtherWhoUseSameCertVulnerableTo(target, detect, null);
	}

	static boolean isOtherWhoUseSameCertVulnerableTo(SegmentMap target,
	                                                 Function<SegmentMap, Boolean> detect,
	                                                 Map<String, Boolean> cache) {

		String serialNumber = (String) target.get("cert_serialNumber").getResult();

		try {
			AtomicBoolean isVul = new AtomicBoolean(false);
			List<String> list = ShodanQueriesHelper.searchIpWithSerial(serialNumber);
			list.forEach(e -> {

				if (cache != null && cache.containsKey(e)) {
					isVul.set(cache.get(e));
					return;
				}

				try {
					String file = ExecutionHelper.runTest(e);
					TargetSegmentMap map = Analyzer.parseFile(file);

					map.forEach((ip, segmentMap) -> {
						if (detect.apply(target)) {
							isVul.set(true);
							if (cache != null) cache.put(ip, true);
						}
					});
				} catch (Exception ex) {
					throw new IllegalArgumentException(TAG + " " + ex.getMessage());
				}
			});

			return isVul.get();
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}
}
