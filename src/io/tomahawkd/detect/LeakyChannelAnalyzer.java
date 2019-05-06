package io.tomahawkd.detect;

import de.rub.nds.tlsattacker.core.workflow.action.MessageAction;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.data.SegmentMap;
import io.tomahawkd.testssl.data.parser.CipherInfo;
import io.tomahawkd.testssl.data.parser.CipherSuite;
import io.tomahawkd.tlsattacker.KeyExchangeTester;

import java.util.List;

public class LeakyChannelAnalyzer {

	private static final Logger logger = Logger.getLogger(LeakyChannelAnalyzer.class);

	private StringBuilder resultText = new StringBuilder();

	public String getResult() {
		return resultText.toString();
	}

	public boolean checkVulnerability(SegmentMap target) {

		resultText = new StringBuilder();

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

	private boolean isRSAUsed(SegmentMap target) {

		resultText.append("\t\t| 1 RSA key exchange is preferred in the highest supported version of TLS: ");
		CipherSuite cipher = (CipherSuite) target.get("cipher_negotiated").getResult();

		boolean preferred = false;
		if (cipher != null) preferred = cipher.getKeyExchange().contains("RSA");
		else logger.critical("cipher not found, assuming false");

		resultText.append(preferred).append("\n");


		resultText.append("\t\t| 2 Downgrade is possible to a version of TLS where RSA key exchange is preferred: ");

		boolean isPossible = false;
		if (cipher != null) {
			isPossible = AnalyzerHelper.downgradeIsPossibleToAVersionOf(target,
					CipherInfo.SSLVersion.TLS1,
					(version, suite, segmentMap) -> {
						if (suite.getKeyExchange().contains("RSA")) {
							if (suite.getCipherForTesting() == null) {
								logger.critical("cipher isn't support by tls attacker, returning false");
								return false;
							}
							List<MessageAction> result = new KeyExchangeTester(segmentMap.getIp())
									.setCipherSuite(suite.getCipherForTesting())
									.setNegotiateVersion(version).initRSA().execute();
							return result.get(result.size() - 1).getMessages().size() > 1;

						} else return false;
					});
		} else {
			logger.critical("cipher not found, assuming false");
		}
		resultText.append(isPossible).append("\n");

		return preferred || isPossible;
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
