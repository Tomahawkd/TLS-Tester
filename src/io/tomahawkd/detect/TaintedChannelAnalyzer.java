package io.tomahawkd.detect;

import de.rub.nds.tlsattacker.core.protocol.message.ECDHEServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.RSAClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ServerHelloMessage;
import de.rub.nds.tlsattacker.core.workflow.action.MessageAction;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.data.SectionType;
import io.tomahawkd.testssl.data.Segment;
import io.tomahawkd.testssl.data.SegmentMap;
import io.tomahawkd.testssl.data.parser.CipherInfo;
import io.tomahawkd.testssl.data.parser.CipherSuite;
import io.tomahawkd.testssl.data.parser.OfferedResult;
import io.tomahawkd.testssl.data.parser.PreservedCipherList;
import io.tomahawkd.tlsattacker.HeartBleedTester;
import io.tomahawkd.tlsattacker.KeyExchangeTester;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaintedChannelAnalyzer {

	private static final Logger logger = Logger.getLogger(TaintedChannelAnalyzer.class);

	private static StringBuilder resultText;

	public static boolean checkVulnerability(SegmentMap target) {

		resultText = new StringBuilder();

		resultText.append("Checking ").append(target.getIp()).append("\n\n");
		resultText.append("GOAL Potential MITM (decryption and modification)\n");
		resultText.append("-----------------------------------------------\n");


		boolean res = canForceRSAKeyExchangeAndDecrypt(target) |
				canLearnTheSessionKeysOfLongLivedSession(target) |
				canForgeRSASignatureInTheKeyEstablishment(target) |
				isHeartBleed(target);


		if (res) logger.warn(resultText);
		else logger.ok(resultText);

		return res;
	}

	private static boolean canForceRSAKeyExchangeAndDecrypt(SegmentMap target) {

		resultText.append("| 1 Force RSA key exchange by modifying ClientHello " +
				"and decrypt it before the handshake times out\n");


		resultText.append("\t& 1 RSA key exchange support in any TLS version: ");
		boolean isSupported = isRSAUsedInAnyVersion(target);
		resultText.append(isSupported).append("\n");


		resultText.append("\t& 2 Fast RSA decryption oracle (Special DROWN or" +
				"Strong Bleichenbacher’s oracle) available on:\n");


		resultText.append("\t\t| 1 This host: ");
		boolean isHost = isHostRSAVulnerable(target);
		resultText.append(isHost).append("\n");


		resultText.append("\t\t| 2 Another host with the same certificate\n")
				.append("\t\t| 3 Another host with the same public RSA key: ");
		boolean isOther = isOtherRSAVulnerable(target);
		resultText.append(isOther).append("\n");


		return isSupported && (isHost && isOther);
	}

	private static boolean canLearnTheSessionKeysOfLongLivedSession(SegmentMap target) {

		resultText.append("| 2 Learn the session keys of a long lived session\n");


		resultText.append("\t& 1 Learn the session keys (Figure 2): ");
		boolean learn = LeakyChannelAnalyzer.checkVulnerability(target);
		resultText.append(learn).append("\n");


		resultText.append("\t& 2 Client resumes the session\n");


		resultText.append("\t\t| 1 Session resumption with tickets\n")
				.append("\t\t| 2 Session resumption with session IDs: ");
		boolean ticket = ((OfferedResult) target.get("sessionresumption_ticket").getResult()).isResult();
		boolean id = ((OfferedResult) target.get("sessionresumption_ID").getResult()).isResult();

		// what we can get from tls attacker is session id
		boolean isResumed = false;
		if (id) {
			String name = (String) target.get("cipher_negotiated").getResult();
			if (name.contains(",")) name = name.split(",")[0].trim();
			CipherSuite cipher = PreservedCipherList.getFromName(name);

			if (cipher != null) {

				logger.info("Testing is session resuming using last ticket");
				// since we only implement these key exchange
				if (cipher.getKeyExchange().contains("RSA")) {
					List<MessageAction> r = new KeyExchangeTester(target.getIp())
							.setCipherSuite(cipher.getCipherForTesting())
							.initRSA(null).execute();

					List<MessageAction> result = new KeyExchangeTester(target.getIp())
							.setCipherSuite(cipher.getCipherForTesting())
							.initRSA(((ServerHelloMessage) r.get(1).getMessages().get(0)).getSessionId())
							.execute();

					if (result.get(result.size() - 1).getMessages().size() > 1) isResumed = true;

				} else if (cipher.getKeyExchange().contains("ECDHE")) {
					List<MessageAction> ec = new KeyExchangeTester(target.getIp())
							.setCipherSuite(cipher.getCipherForTesting())
							.initECDHE(null).execute();

					List<MessageAction> result = new KeyExchangeTester(target.getIp())
							.setCipherSuite(cipher.getCipherForTesting())
							.initECDHE(((ServerHelloMessage) ec.get(1).getMessages().get(0)).getSessionId())
							.execute();

					if (result.get(result.size() - 1).getMessages().size() > 1) isResumed = true;
				}
			} else logger.critical("Null pointer of cipher found");
		}

		boolean isResumption = (ticket || id) && isResumed;
		resultText.append(isResumption).append("\n");


		return learn && isResumption;
	}

	private static boolean canForgeRSASignatureInTheKeyEstablishment(SegmentMap target) {

		resultText.append("| 3 Forge an RSA signature in the key establishment");


		resultText.append("\t& 1 Fast RSA signature oracle (Strong Bleichenbacher’s oracle) is available on:\n");


		resultText.append("\t\t| 1 This host: ");
		boolean thisRobot = AnalyzerHelper.isVulnerableTo(target, VulnerabilityTags.ROBOT);
		resultText.append(thisRobot).append("\n");


		resultText.append("\t\t| 2 Another host with the same certificate\n")
				.append("\t\t| 3 Another host with the same public RSA key\n")
				.append("\t\t| 4 A host with a certificate where the " +
						"Subject Alternative Names (SAN) match this host: ");
		boolean robot = AnalyzerHelper.isOtherWhoUseSameCertVulnerableTo(target, VulnerabilityTags.ROBOT);
		resultText.append(robot).append("\n");


		resultText.append("\t& 2 The same RSA key is used for RSA key exchange " +
				"and RSA signature in ECDHE key establishment: ");
		ArrayList<RSAClientKeyExchangeMessage> rsa = new ArrayList<>();
		ArrayList<ECDHEServerKeyExchangeMessage> ecdhe = new ArrayList<>();

		logger.info("Testing RSA key duplication");

		List<Segment> list = target.getByType(SectionType.CIPHER_ORDER);
		for (Segment current : list) {

			((CipherInfo) current.getResult()).getCipher().getList().forEach(e -> {
				if (e.getKeyExchange().contains("RSA")) {
					List<MessageAction> r = new KeyExchangeTester(target.getIp())
							.setCipherSuite(e.getCipherForTesting())
							.initRSA(null).execute();

					rsa.add((RSAClientKeyExchangeMessage) r.get(2).getMessages().get(0));
				} else if (e.getKeyExchange().contains("ECDHE")) {
					List<MessageAction> ec = new KeyExchangeTester(target.getIp())
							.setCipherSuite(e.getCipherForTesting())
							.initECDHE(null).execute();

					ecdhe.add((ECDHEServerKeyExchangeMessage) ec.get(2).getMessages().get(0));
				}
			});
		}

		AtomicBoolean isSame = new AtomicBoolean(false);

		rsa.forEach(r -> ecdhe.forEach(e -> {
			if (e.getPublicKey().equals(r.getPublicKey())) isSame.set(true);
		}));

		resultText.append(isSame.get()).append("\n");


		return (thisRobot && robot) && isSame.get();
	}

	private static boolean isHeartBleed(SegmentMap target) {
		resultText.append("| 4 Private key leak due to the Heartbleed bug: ");
		boolean heartbleed = AnalyzerHelper.isVulnerableTo(target, VulnerabilityTags.HEARTBLEED);

		// do further test
		heartbleed = heartbleed | new HeartBleedTester().test(target.getIp());
		resultText.append(heartbleed).append("\n");

		return heartbleed;
	}


	private static boolean isRSAUsedInAnyVersion(SegmentMap target) {
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
