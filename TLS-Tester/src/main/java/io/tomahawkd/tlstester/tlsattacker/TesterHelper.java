package io.tomahawkd.tlstester.tlsattacker;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.StarttlsDelegate;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.constants.StarttlsType;
import io.tomahawkd.tlstester.InternalNamespaces;
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

	public static void setStarttlsProtocol(Config config, String protocol) {
		StarttlsDelegate delegate = new StarttlsDelegate();
		switch (protocol) {
			case InternalNamespaces.Protocol.FTP: {
				delegate.setStarttlsType(StarttlsType.FTP);
				break;
			}

			case InternalNamespaces.Protocol.IMAP: {
				delegate.setStarttlsType(StarttlsType.IMAP);
				break;
			}

			case InternalNamespaces.Protocol.POP3: {
				delegate.setStarttlsType(StarttlsType.POP3);
				break;
			}

			case InternalNamespaces.Protocol.SMTP: {
				delegate.setStarttlsType(StarttlsType.SMTP);
				break;
			}

			default:
				break;
		}
		delegate.applyDelegate(config);
	}
}
