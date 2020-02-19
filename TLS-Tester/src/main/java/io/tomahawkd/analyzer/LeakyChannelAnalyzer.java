package io.tomahawkd.analyzer;

import de.rub.nds.tlsattacker.core.workflow.action.MessageAction;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.data.TargetInfo;
import io.tomahawkd.database.Record;
import io.tomahawkd.testssl.data.SegmentMap;
import io.tomahawkd.testssl.data.parser.CipherInfo;
import io.tomahawkd.testssl.data.parser.CipherSuite;
import io.tomahawkd.tlsattacker.KeyExchangeTester;

import java.util.List;

@Record(column = "leaky")
public class LeakyChannelAnalyzer extends AbstractAnalyzer {

	private static final Logger logger = Logger.getLogger(LeakyChannelAnalyzer.class);

	public static final int RSA_KEY_EXCHANGE_OFFLINE = 0;
	public static final int RSA_KEY_EXCHANGE_USED = 1;
	public static final int RSA_KEY_EXCHANGE_PREFERRED = 2;
	public static final int RSA_KEY_EXCHANGE_DOWNGRADE = 3;
	public static final int RSA_DECRYPTION = 4;
	public static final int RSA_DECRYPTION_HOST = 5;
	public static final int RSA_DECRYPTION_OTHER = 6;
	public static final int TREE_LENGTH = 7;

	LeakyChannelAnalyzer() {
		super(TREE_LENGTH);
	}

	@Override
	public boolean getResult(TreeCode code) {
		return code.get(RSA_KEY_EXCHANGE_OFFLINE);
	}

	@Override
	public String getResultDescription() {

		return "GOAL Learn the session keys (allows decryption)\n" +
				"-----------------------------------------------\n" +
				"| 1 Decrypt RSA key exchange offline: " + code.get(RSA_KEY_EXCHANGE_OFFLINE) + "\n" +
				"\t& 1 RSA key exchange is used: " + code.get(RSA_KEY_EXCHANGE_USED) + "\n" +
				"\t\t| 1 RSA key exchange is preferred in the highest supported version of TLS: "
				+ code.get(RSA_KEY_EXCHANGE_PREFERRED) + "\n" +
				"\t\t| 2 Downgrade is possible to a version of TLS where RSA key exchange is preferred: "
				+ code.get(RSA_KEY_EXCHANGE_DOWNGRADE) + "\n" +
				"\t& 2 RSA decryption oracle (DROWN or Strong Bleichenbacher’s oracle) is available on: " +
				code.get(RSA_DECRYPTION) + "\n" +
				"\t\t| 1 This host: " + code.get(RSA_DECRYPTION_HOST) + "\n" +
				"\t\t| 2 Another host with the same certificate\n" +
				"\t\t| 3 Another host with the same public RSA key: " + code.get(RSA_DECRYPTION_OTHER) + "\n";
	}

	@Override
	public void analyze(TargetInfo info) {

		logger.info("Start test leaky channel on " + info.getIp());

		boolean rsaUsed = isRSAUsed(info.getTargetData());
		code.set(rsaUsed, RSA_KEY_EXCHANGE_USED);

		boolean isVul = isHostRSAVulnerable(info.getTargetData());
		code.set(isVul, RSA_DECRYPTION_HOST);

		boolean isOther = isOtherRSAVulnerable(info.getTargetData());
		code.set(isOther, RSA_DECRYPTION_OTHER);

		boolean rsaExploitable = isVul || isOther;
		code.set(rsaExploitable, RSA_DECRYPTION);

		boolean res = rsaUsed && rsaExploitable;
		code.set(res, RSA_KEY_EXCHANGE_OFFLINE);
	}

	@Override
	public void postAnalyze(TargetInfo info) {
		logger.debug("Result: " + code);
		String result = "\n" + getResultDescription();
		if (getResult()) logger.warn(result);
		else logger.ok(result);
	}

	private boolean isRSAUsed(SegmentMap target) {

		CipherSuite cipher = (CipherSuite) target.get("cipher_negotiated").getResult();

		boolean preferred = false;
		if (cipher != null) preferred = cipher.getKeyExchange().contains("RSA");
		else logger.critical("cipher not found, assuming false");
		code.set(preferred, RSA_KEY_EXCHANGE_PREFERRED);

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
		code.set(isPossible, RSA_KEY_EXCHANGE_DOWNGRADE);

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
