package io.tomahawkd.detect;

import de.rub.nds.tlsattacker.core.protocol.message.ECDHEServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
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
import io.tomahawkd.tlsattacker.ConnectionTester;
import io.tomahawkd.tlsattacker.KeyExchangeTester;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaintedChannelAnalyzer {

	private static final Logger logger = Logger.getLogger(TaintedChannelAnalyzer.class);

	public static final int FORCE_RSA_KEY_EXCHANGE = 0;
	public static final int RSA_KEY_EXCHANGE_SUPPORTED = 1;
	public static final int RSA_DECRYPTION = 2;
	public static final int RSA_DECRYPTION_HOST = 3;
	public static final int RSA_DECRYPTION_OTHER = 4;
	public static final int LEARN_LONG_LIVE_SESSION = 5;
	public static final int LEARN_SESSION_KEY = 6;
	public static final int CLIENT_RESUMES_SESSION = 7;
	public static final int RESUMPTION_WITH_TICKETS = 8;
	public static final int RESUMPTION_WITH_IDS = 9;
	public static final int FORGE_RSA_SIGN = 10;
	public static final int RSA_SIGN = 11;
	public static final int RSA_SIGN_HOST = 12;
	public static final int RSA_SIGN_OTHER = 13;
	public static final int SAME_RSA_KEY_AND_SIGN = 14;
	public static final int HEARTBLEED = 15;
	public static final int TREE_LENGTH = 16;

	private final TreeCode code = new TreeCode(TREE_LENGTH);

	public TaintedChannelAnalyzer(boolean leakyResult) {
		code.set(leakyResult, LEARN_SESSION_KEY);
	}

	public String getResult() {

		return "GOAL Potential MITM (decryption and modification)\n" +
				"-----------------------------------------------\n" +
				"| 1 Force RSA key exchange by modifying Client Hello and decrypt it before the handshake times out: "
				+ code.get(FORCE_RSA_KEY_EXCHANGE) + "\n" +
				"\t& 1 RSA key exchange support in any TLS version: " + code.get(RSA_KEY_EXCHANGE_SUPPORTED) + "\n" +
				"\t& 2 Fast RSA decryption oracle (Special DROWN or Strong Bleichenbacher’s oracle) available on: "
				+ code.get(RSA_DECRYPTION) + "\n" +
				"\t\t| 1 This host: " + code.get(RSA_DECRYPTION_HOST) + "\n" +
				"\t\t| 2 Another host with the same certificate\n" +
				"\t\t| 3 Another host with the same public RSA key: " + code.get(RSA_DECRYPTION_OTHER) + "\n" +
				"| 2 Learn the session keys of a long lived session: " + code.get(LEARN_LONG_LIVE_SESSION) + "\n" +
				"\t& 1 Learn the session keys (Figure 2): " + code.get(LEARN_SESSION_KEY) + "\n" +
				"\t& 2 Client resumes the session: " + code.get(CLIENT_RESUMES_SESSION) + "\n" +
				"\t\t| 1 Session resumption with tickets: " + code.get(RESUMPTION_WITH_TICKETS) + "\n" +
				"\t\t| 2 Session resumption with session IDs: " + code.get(RESUMPTION_WITH_IDS) + "\n" +
				"| 3 Forge an RSA signature in the key establishment: " + code.get(FORGE_RSA_SIGN) + "\n" +
				"\t& 1 Fast RSA signature oracle (Strong Bleichenbacher’s oracle) is available on: "
				+ code.get(RSA_SIGN) + "\n" +
				"\t\t| 1 This host: " + code.get(RSA_SIGN_HOST) + "\n" +
				"\t\t| 2 Another host with the same certificate\n" +
				"\t\t| 3 Another host with the same public RSA key\n" +
				"\t\t| 4 A host with a certificate where the Subject Alternative Names (SAN) match this host: "
				+ code.get(RSA_SIGN_OTHER) + "\n" +
				"\t& 2 The same RSA key is used for RSA key exchange and RSA signature in ECDHE key establishment: "
				+ code.get(SAME_RSA_KEY_AND_SIGN) + "\n" +
				"| 4 Private key leak due to the Heartbleed bug: " + code.get(HEARTBLEED) + "\n";
	}

	public TreeCode getCode() {
		return code;
	}

	public static void update(TreeCode code) {

		code.set(code.get(RSA_DECRYPTION_HOST) || code.get(RSA_DECRYPTION_OTHER), RSA_DECRYPTION);
		code.set(code.get(RSA_KEY_EXCHANGE_SUPPORTED) && code.get(RSA_DECRYPTION), FORCE_RSA_KEY_EXCHANGE);

		code.set(code.get(RESUMPTION_WITH_TICKETS) || code.get(RESUMPTION_WITH_IDS), CLIENT_RESUMES_SESSION);
		code.set(code.get(LEARN_SESSION_KEY) && code.get(CLIENT_RESUMES_SESSION), LEARN_LONG_LIVE_SESSION);

		code.set(code.get(RSA_SIGN_HOST) || code.get(RSA_SIGN_OTHER), RSA_SIGN);
		code.set(code.get(RSA_SIGN) && code.get(SAME_RSA_KEY_AND_SIGN), FORGE_RSA_SIGN);
	}

	public boolean checkVulnerability(SegmentMap target) {

		boolean force = canForceRSAKeyExchangeAndDecrypt(target);
		code.set(force, FORCE_RSA_KEY_EXCHANGE);

		boolean learn = canLearnTheSessionKeysOfLongLivedSession(target);
		code.set(learn, LEARN_LONG_LIVE_SESSION);

		boolean forge = canForgeRSASignatureInTheKeyEstablishment(target);
		code.set(forge, FORGE_RSA_SIGN);

		boolean heartbleed = AnalyzerHelper.isVulnerableTo(target, VulnerabilityTags.HEARTBLEED);
		code.set(heartbleed, HEARTBLEED);

		boolean res = force || learn || forge || heartbleed;

		String result = "\n" + getResult();
		if (res) logger.warn(result);
		else logger.ok(result);

		return res;
	}

	private boolean canForceRSAKeyExchangeAndDecrypt(SegmentMap target) {

		boolean isSupported = isRSAUsedInAnyVersion(target);
		code.set(isSupported, RSA_KEY_EXCHANGE_SUPPORTED);

		boolean isHost = isHostRSAVulnerable(target);
		code.set(isHost, RSA_DECRYPTION_HOST);

		boolean isOther = isOtherRSAVulnerable(target);
		code.set(isOther, RSA_DECRYPTION_OTHER);

		boolean isVul = isHost || isOther;
		code.set(isVul, RSA_DECRYPTION);

		return isSupported && isVul;
	}

	private boolean canLearnTheSessionKeysOfLongLivedSession(SegmentMap target) {

		boolean ticket = ((OfferedResult) target.get("sessionresumption_ticket").getResult()).isResult();
		code.set(ticket, RESUMPTION_WITH_TICKETS);

		boolean id = ((OfferedResult) target.get("sessionresumption_ID").getResult()).isResult();

		// what we can get from tls attacker is session id
		boolean isResumed = false;
		if (id) {
			CipherSuite cipher = (CipherSuite) target.get("cipher_negotiated").getResult();

			if (cipher != null && cipher.getCipherForTesting() != null) {

				logger.info("Testing is session resuming using last ticket");

				List<ProtocolMessage> r = new ConnectionTester(target.getIp())
						.setCipherSuite(cipher.getCipherForTesting())
						.execute().getHandShakeMessages();

				if (!r.isEmpty()) {
					List<ProtocolMessage> result = new ConnectionTester(target.getIp())
							.setCipherSuite(cipher.getCipherForTesting())
							.execute(((ServerHelloMessage) r.get(0)).getSessionId()).getHandShakeMessages();

					if (result.size() > 1) isResumed = true;
				} else logger.critical("did not receive server hello message");

			} else logger.critical("Null pointer of cipher found");
		}

		boolean idResume = id && isResumed;
		code.set(idResume, RESUMPTION_WITH_IDS);

		boolean isResumption = ticket || idResume;
		code.set(isResumption, CLIENT_RESUMES_SESSION);

		return code.get(LEARN_SESSION_KEY) && isResumption;
	}

	private boolean canForgeRSASignatureInTheKeyEstablishment(SegmentMap target) {

		boolean thisRobot = AnalyzerHelper.isVulnerableTo(target, VulnerabilityTags.ROBOT);
		code.set(thisRobot, RSA_SIGN_HOST);

		boolean robot = AnalyzerHelper.isOtherWhoUseSameCertVulnerableTo(target, VulnerabilityTags.ROBOT);
		code.set(robot, RSA_SIGN_OTHER);

		boolean rsaSign = thisRobot || robot;
		code.set(rsaSign, RSA_SIGN);

		ArrayList<RSAClientKeyExchangeMessage> rsa = new ArrayList<>();
		ArrayList<ECDHEServerKeyExchangeMessage> ecdhe = new ArrayList<>();

		logger.info("Testing RSA key duplication");

		List<Segment> list = target.getByType(SectionType.CIPHER_ORDER);
		for (Segment current : list) {

			((CipherInfo) current.getResult()).getCipher().getList().forEach(e -> {
				if (e.getCipherForTesting() == null) return;
				if (e.getKeyExchange().contains("RSA")) {
					List<MessageAction> r = new KeyExchangeTester(target.getIp())
							.setCipherSuite(e.getCipherForTesting()).initRSA().execute();

					rsa.add((RSAClientKeyExchangeMessage) r.get(2).getMessages().get(0));
				} else if (e.getKeyExchange().contains("ECDHE")) {
					List<MessageAction> ec = new KeyExchangeTester(target.getIp())
							.setCipherSuite(e.getCipherForTesting()).initECDHE().execute();

					ecdhe.add((ECDHEServerKeyExchangeMessage) ec.get(2).getMessages().get(0));
				}
			});
		}

		AtomicBoolean isSame = new AtomicBoolean(false);

		rsa.forEach(r -> ecdhe.forEach(e -> {
			if (e.getPublicKey().equals(r.getPublicKey())) isSame.set(true);
		}));
		code.set(isSame.get(), SAME_RSA_KEY_AND_SIGN);

		return rsaSign && isSame.get();
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
