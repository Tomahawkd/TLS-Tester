package io.tomahawkd.detect;

import io.tomahawkd.common.ShodanQueriesHelper;
import io.tomahawkd.testssl.Analyzer;
import io.tomahawkd.testssl.ExecutionHelper;
import io.tomahawkd.testssl.data.SectionType;
import io.tomahawkd.testssl.data.SegmentMap;
import io.tomahawkd.testssl.data.TargetSegmentMap;
import io.tomahawkd.testssl.data.parser.CipherInfo;
import io.tomahawkd.testssl.data.parser.OfferedResult;
import io.tomahawkd.testssl.data.parser.PreservedCipherList;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class LeakyChannelAnalyzer {

	public static final String TAG = "[LeakyChannelAnalyzer]";

	private static Map<String, Boolean> cache = new HashMap<>();

	public static boolean checkVulnerable(SegmentMap target, boolean shouldContinue) {
		System.out.println("Checking " + target.getIp());
		var isVul = isHostRSAVulnerable(target);
		cache.put(target.getIp(), isVul);
		return isRSAUsed(target) && (isVul || isOtherRSAVulnerable(target, shouldContinue));
	}

	private static boolean isRSAUsed(SegmentMap target) {

		// Part I
		var name = (String) target.get("cipher_negotiated").getResult();
		if (name.contains(",")) name = name.split(",")[0].trim();
		var cipher = PreservedCipherList.getFromName(name);
		if (cipher == null) throw new IllegalArgumentException(TAG + " Cipher not found");
		var preferred = cipher.getKeyExchange().contains("RSA");

		// Part II
		var list = target.getByType(SectionType.CIPHER_ORDER);
		var count = 0;
		for (var current : list) {
			AtomicBoolean hasRSA = new AtomicBoolean(false);
			((CipherInfo) current.getResult()).getCipher().getList().forEach(e -> {
				if (e.getKeyExchange().contains("RSA")) hasRSA.set(true);
			});
			if (hasRSA.get()) count++;
		}

		return preferred || count > 0;
	}

	private static boolean isHostRSAVulnerable(SegmentMap target) {
		var robot = target.get("ROBOT").getResult();
		if (robot == null) throw new IllegalArgumentException("No vulnerability tag");
		var drown = target.get("DROWN").getResult();
		if (drown == null) throw new IllegalArgumentException("No vulnerability tag");

		return ((OfferedResult) robot).isResult() || ((OfferedResult) drown).isResult();
	}

	private static boolean isOtherRSAVulnerable(SegmentMap target, boolean shouldContinue) {

		// should not continue for dead loop
		if (!shouldContinue) return false;

		var serialNumber = (String) target.get("cert_serialNumber").getResult();

		// According to the source code, this method put the result into the map,
		// so that we do not need to put it manually except this host self.
		try {
			AtomicBoolean isVul = new AtomicBoolean(false);
			var list = ShodanQueriesHelper.searchIpWithSerial(serialNumber);
			list.forEach(e -> isVul.set(cache.computeIfAbsent(e, k -> {

				var singleVul = new AtomicBoolean(false);

				try {
					var file = ExecutionHelper.runTest(e);
					TargetSegmentMap map = Analyzer.parseFile(file);

					map.forEach((ip, segmentMap) -> {
						if (isHostRSAVulnerable(segmentMap)) singleVul.set(true);
					});
				} catch (IOException | InterruptedException ex) {
					throw new IllegalArgumentException(ex.getMessage());
				}

				return singleVul.get();
			})));

			return isVul.get();
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}
}
