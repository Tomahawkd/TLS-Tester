package io.tomahawkd.detect;

import io.tomahawkd.common.ShodanQueriesHelper;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.ExecutionHelper;
import io.tomahawkd.testssl.data.SectionType;
import io.tomahawkd.testssl.data.Segment;
import io.tomahawkd.testssl.data.SegmentMap;
import io.tomahawkd.testssl.data.TargetSegmentMap;
import io.tomahawkd.testssl.data.parser.CommonParser;
import io.tomahawkd.testssl.data.parser.OfferedResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

class AnalyzerHelper {

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
					logger.debug("Ip " + e + " matched in cache");
					isVul.set(cache.get(e));
					return;
				}

				logger.debug("Ip " + e + " not matched in cache");

				try {
					String file = ExecutionHelper.runTest(e);
					TargetSegmentMap map = CommonParser.parseFile(file);

					map.forEach((ip, segmentMap) -> {
						if (detect.apply(target)) {
							isVul.set(true);
							if (cache != null) cache.put(ip, true);
						}
					});
				} catch (Exception ex) {
					logger.fatal(ex.getMessage());
					throw new IllegalArgumentException(ex.getMessage());
				}
			});

			return isVul.get();
		} catch (Exception e) {
			logger.fatal(e.getMessage());
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	public static class Cache {

		private Map<String, Map<String, Boolean>> cache = new HashMap<>();

		boolean containsKey(String vulnerability, String ip) {
			return cache.containsKey(vulnerability) && cache.get(vulnerability).containsKey(ip);
		}

		// Be aware that the default value will be in put in to the map if absent
		boolean getOrDefault(String vulnerability, String ip, Supplier<Boolean> defaultValue) {
			return cache.computeIfAbsent(vulnerability, vul -> {
				logger.debug("Vulnerability tag" + vulnerability + "not found");
				return new HashMap<>();
			}).computeIfAbsent(ip, s -> {
				logger.debug("Ip " + ip + " not matched in cache");
				return defaultValue.get();
			});
		}

		boolean getOrDefault(String vulnerability, String ip, boolean isVulnerable) {
			return getOrDefault(vulnerability, ip, () -> isVulnerable);
		}

		void put(String vulnerability, String ip, boolean isVulnerable) {
			cache.computeIfAbsent(vulnerability, vul -> {
				logger.debug("Vulnerability tag" + vulnerability + "not found");
				return new HashMap<>();
			}).put(ip, isVulnerable);
		}
	}

}
