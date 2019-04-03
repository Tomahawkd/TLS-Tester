package io.tomahawkd.detect;

import io.tomahawkd.testssl.data.SectionType;
import io.tomahawkd.testssl.data.SegmentMap;
import io.tomahawkd.testssl.data.parser.CipherInfo;
import io.tomahawkd.testssl.data.parser.PreservedCipherList;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class LeakyChannelAnalyzer {

	public static final String TAG = "[LeakyChannelAnalyzer]";

	private static Map<String, Boolean> cache = new HashMap<>();

	public static boolean checkVulnerable(SegmentMap target) {
		System.out.println("Checking " + target.getIp());
		var isVul = isHostRSAVulnerable(target);
		cache.put(target.getIp(), isVul);
		return isRSAUsed(target) && (isVul || isOtherRSAVulnerable(target));
	}

	static boolean isRSAUsedInAnyVersion(SegmentMap target) {
		var list = target.getByType(SectionType.CIPHER_ORDER);
		var count = 0;
		for (var current : list) {

			AtomicBoolean hasRSA = new AtomicBoolean(false);
			((CipherInfo) current.getResult()).getCipher().getList().forEach(e -> {
				if (e.getKeyExchange().contains("RSA")) hasRSA.set(true);
			});
			if (hasRSA.get()) count++;
		}

		return count > 0;
	}

	private static boolean isRSAUsed(SegmentMap target) {

		// Part I
		var name = (String) target.get("cipher_negotiated").getResult();
		if (name.contains(",")) name = name.split(",")[0].trim();
		var cipher = PreservedCipherList.getFromName(name);
		if (cipher == null) throw new IllegalArgumentException(TAG + " Cipher not found");
		var preferred = cipher.getKeyExchange().contains("RSA");

		return preferred || isRSAUsedInAnyVersion(target);
	}

	static boolean isHostRSAVulnerable(SegmentMap target) {
		return AnalyzerHelper.isVulnerableTo(target, "ROBOT") ||
				AnalyzerHelper.isVulnerableTo(target, "DROWN");
	}

	private static boolean isOtherRSAVulnerable(SegmentMap target) {
		return AnalyzerHelper.isOtherWhoUseSameCertVulnerableTo(target,
				LeakyChannelAnalyzer::isHostRSAVulnerable, cache);
	}
}
