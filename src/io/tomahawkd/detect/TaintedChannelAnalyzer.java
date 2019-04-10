package io.tomahawkd.detect;

import de.rub.nds.tlsattacker.core.protocol.message.ECDHEServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.RSAClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ServerHelloMessage;
import de.rub.nds.tlsattacker.core.workflow.action.MessageAction;
import io.tomahawkd.testssl.data.SectionType;
import io.tomahawkd.testssl.data.Segment;
import io.tomahawkd.testssl.data.SegmentMap;
import io.tomahawkd.testssl.data.parser.CipherInfo;
import io.tomahawkd.testssl.data.parser.CipherSuite;
import io.tomahawkd.testssl.data.parser.OfferedResult;
import io.tomahawkd.testssl.data.parser.PreservedCipherList;
import io.tomahawkd.tlsattacker.KeyExchangeTester;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaintedChannelAnalyzer {

	public static boolean checkVulnerable(SegmentMap target) {
		return canForceRSAKeyExchangeAndDecrypt(target) ||
				canLearnTheSessionKeysOfLongLivedSession(target) ||
				canForgeRSASignatureInTheKeyEstablishment(target) ||
				AnalyzerHelper.isVulnerableTo(target, "heartbleed");
	}

	private static boolean canForceRSAKeyExchangeAndDecrypt(SegmentMap target) {
		return LeakyChannelAnalyzer.isRSAUsedInAnyVersion(target) &&
				LeakyChannelAnalyzer.isHostRSAVulnerable(target);
	}

	private static boolean canLearnTheSessionKeysOfLongLivedSession(SegmentMap target) {

		boolean ticket = ((OfferedResult) target.get("sessionresumption_ticket").getResult()).isResult();
		boolean id = ((OfferedResult) target.get("sessionresumption_ID").getResult()).isResult();

		// what we can get from tls attacker is session id

		boolean isResumed = false;
		if (id) {
			String name = (String) target.get("cipher_negotiated").getResult();
			if (name.contains(",")) name = name.split(",")[0].trim();
			CipherSuite cipher = PreservedCipherList.getFromName(name);

			if (cipher != null) {

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
			}
		}

		return LeakyChannelAnalyzer.checkVulnerable(target) && (ticket || id) && isResumed;
	}

	private static boolean canForgeRSASignatureInTheKeyEstablishment(SegmentMap target) {

		// part I
		boolean robot = AnalyzerHelper.isOtherWhoUseSameCertVulnerableTo(target,
				TaintedChannelAnalyzer::isVulnerableToROBOT);

		ArrayList<RSAClientKeyExchangeMessage> rsa = new ArrayList<>();
		ArrayList<ECDHEServerKeyExchangeMessage> ecdhe = new ArrayList<>();

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

		rsa.forEach(r -> {
			ecdhe.forEach(e -> {
				if (e.getPublicKey().equals(r.getPublicKey())) isSame.set(true);
			});
		});

		return robot && isSame.get();
	}

	private static boolean isVulnerableToROBOT(SegmentMap target) {
		return AnalyzerHelper.isVulnerableTo(target, "ROBOT");
	}
}
