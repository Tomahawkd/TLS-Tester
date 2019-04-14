package io.tomahawkd.detect;

import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.data.SectionType;
import io.tomahawkd.testssl.data.Segment;
import io.tomahawkd.testssl.data.SegmentMap;
import io.tomahawkd.testssl.data.parser.CipherInfo;
import io.tomahawkd.testssl.data.parser.CipherSuite;
import io.tomahawkd.testssl.data.parser.PreservedCipherList;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LeakyChannelAnalyzer {

	private static final Logger logger = Logger.getLogger(LeakyChannelAnalyzer.class);

	private static StringBuilder resultText;

	public static boolean checkVulnerability(SegmentMap target) {

		resultText = new StringBuilder();

		resultText.append("Checking ").append(target.getIp()).append("\n\n");
		resultText.append("GOAL Learn the session keys (allows decryption)\n");
		resultText.append("-----------------------------------------------\n");
		resultText.append("| 1 Decrypt RSA key exchange offline\n");

		resultText.append("\t& 1 RSA key exchange is used\n");
		boolean isRSA = isRSAUsed(target);

		resultText.append("\t& 2 RSA decryption oracle (DROWN or Strong Bleichenbacher’s oracle) is available on:\n");

		resultText.append("\t\t| 1 This host: ");
		boolean isVul = isHostRSAVulnerable(target);
		resultText.append(isVul).append("\n");

		resultText.append("\t\t| 2 Another host with the same certificate\n")
				.append("\t\t| 3 Another host with the same public RSA key: ");
		boolean isOther = isOtherRSAVulnerable(target);
		resultText.append(isOther).append("\n");

		boolean res = isRSA && (isVul || isOther);

		if (res) logger.warn(resultText);
		else logger.ok(resultText);
		return res;
	}

	private static boolean isRSAUsed(SegmentMap target) {

		resultText.append("\t\t| 1 RSA key exchange is preferred in the highest supported version of TLS: ");
		String name = (String) target.get("cipher_negotiated").getResult();
		if (name.contains(",")) name = name.split(",")[0].trim();
		CipherSuite cipher = PreservedCipherList.getFromName(name);
		if (cipher == null) {
			logger.fatal("Cipher " + name + " not found");
			throw new IllegalArgumentException("Cipher not found");
		}

		boolean preferred = cipher.getKeyExchange().contains("RSA");
		resultText.append(preferred).append("\n");


		resultText.append("\t\t| 2 Downgrade is possible to a version of TLS where RSA key exchange is preferred: ");
		boolean isUsedInAny = isRSAUsedInAnyVersion(target);
		resultText.append(isUsedInAny).append("\n");

		return preferred || isUsedInAny;
	}

	static boolean isRSAUsedInAnyVersion(SegmentMap target) {
		List<Segment> list = target.getByType(SectionType.CIPHER_ORDER);
		int count = 0;
		for (Segment current : list) {

			AtomicBoolean hasRSA = new AtomicBoolean(false);
			((CipherInfo) current.getResult()).getCipher().getList().forEach(e -> {
				if (e.getKeyExchange().contains("RSA")) hasRSA.set(true);
			});
			if (hasRSA.get()) count++;
		}

		return count > 0;
	}

	private static boolean isHostRSAVulnerable(SegmentMap target) {
		return AnalyzerHelper.isVulnerableTo(target, VulnerabilityTags.ROBOT) ||
				AnalyzerHelper.isVulnerableTo(target, VulnerabilityTags.DROWN);
	}

	private static boolean isOtherRSAVulnerable(SegmentMap target) {
		return AnalyzerHelper.isOtherWhoUseSameCertVulnerableTo(target, VulnerabilityTags.ROBOT) ||
				AnalyzerHelper.isOtherWhoUseSameCertVulnerableTo(target, VulnerabilityTags.DROWN);
	}
}
