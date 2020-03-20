package io.tomahawkd.tlstester.tlsattacker;

import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import io.tomahawkd.tlstester.data.testssl.parser.CipherInfo;

public class TesterHelper {


	public static ProtocolVersion getVersionForTest(CipherInfo.SSLVersion version) {
		switch (version) {
			case SSLv2: return ProtocolVersion.SSL2;
			case SSLv3: return ProtocolVersion.SSL3;
			case TLS1: return ProtocolVersion.TLS10;
			case TLS1_1: return ProtocolVersion.TLS11;
			case TLS1_3: return ProtocolVersion.TLS13;
			case TLS1_2:
			default: return ProtocolVersion.TLS12;
		}
	}
}
