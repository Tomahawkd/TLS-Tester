package io.tomahawkd.detect;

import io.tomahawkd.common.PythonRunner;
import io.tomahawkd.testssl.Analyzer;
import io.tomahawkd.testssl.ExecutionHelper;
import io.tomahawkd.testssl.data.parser.CipherInfo;
import io.tomahawkd.testssl.data.parser.CommonParser;
import io.tomahawkd.testssl.data.SectionType;
import io.tomahawkd.testssl.data.SegmentMap;
import io.tomahawkd.testssl.data.TargetSegmentMap;
import io.tomahawkd.testssl.data.parser.OfferedResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class LeakyChannelAnalyzer {

	private static Map<String, Boolean> cache = new HashMap<>();

	public static boolean checkVulnerable(SegmentMap target, boolean shouldContinue) {
		System.out.println("Checking " + target.getIp());
		var isVul = isHostRSAVulnerable(target);
		cache.put(target.getIp(), isVul);
		return isRSAUsed(target) && (isVul || isOtherRSAVulnerable(target, shouldContinue));
	}

	// TODO rsa key exchange judgement
	private static boolean isRSAUsed(SegmentMap target) {

		// Part I
		var preferred = ((String) target.get("cipher_negotiated").getResult()).contains("RSA");

		// Part II
		var list = target.getByType(SectionType.CIPHER_ORDER);
		var count = 0;
		for (var current : list) {
			AtomicBoolean hasRSA = new AtomicBoolean(false);
			((CipherInfo) current.getResult()).getCipher().getList().forEach(e -> {
				if (e.contains("RSA")) hasRSA.set(true);
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

		var fingerprint = (String) target.get("cert_fingerprintSHA256").getResult();

		// According to the source code, this method put the result into the map,
		// so that we do not need to put it manually except this host self.
		try {
			AtomicBoolean isVul = new AtomicBoolean(false);
			var list = PythonRunner.searchForSameCert(fingerprint);
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
