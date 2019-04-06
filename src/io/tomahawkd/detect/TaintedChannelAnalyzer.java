package io.tomahawkd.detect;

import io.tomahawkd.testssl.data.SectionType;
import io.tomahawkd.testssl.data.Segment;
import io.tomahawkd.testssl.data.SegmentMap;
import io.tomahawkd.testssl.data.parser.CipherInfo;
import io.tomahawkd.testssl.data.parser.OfferedResult;

import java.util.List;

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
		return LeakyChannelAnalyzer.checkVulnerable(target) && (ticket || id);
	}

	private static boolean canForgeRSASignatureInTheKeyEstablishment(SegmentMap target) {

		// part I
		boolean robot = AnalyzerHelper.isOtherWhoUseSameCertVulnerableTo(target,
				TaintedChannelAnalyzer::isVulnerableToROBOT);

		return robot;
	}

	private static boolean isVulnerableToROBOT(SegmentMap target) {
		return AnalyzerHelper.isVulnerableTo(target, "ROBOT");
	}
}
