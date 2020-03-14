package io.tomahawkd.tlstester.testssl.data.parser;

import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import io.tomahawkd.tlstester.common.log.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

public class CipherInfo {

	public static final String splitSign = "~~~";

	private static final Logger logger = Logger.getLogger(CipherInfo.class);

	public enum SSLVersion {
		SSLv2(0),
		SSLv3(1),
		TLS1(2),
		TLS1_1(3),
		TLS1_2(4),
		TLS1_3(5),
		UNKNOWN(-1);

		private int level;

		SSLVersion(int level) {
			this.level = level;
		}

		public int getLevel() {
			return level;
		}

		public ProtocolVersion getVersionForTest() {
			switch (this) {
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

	private static Map<String, CipherInfo.SSLVersion> sslVersionMap = new LinkedHashMap<>();

	static {
		sslVersionMap.put("SSLv2", SSLVersion.SSLv2);
		sslVersionMap.put("SSLv3", SSLVersion.SSLv3);
		sslVersionMap.put("TLSv1", SSLVersion.TLS1);
		sslVersionMap.put("TLSv1_1", SSLVersion.TLS1_1);
		sslVersionMap.put("TLSv1_2", SSLVersion.TLS1_2);
		sslVersionMap.put("TLSv1_3", SSLVersion.TLS1_3);
	}

	private SSLVersion sslVersion;
	private CipherSuiteSet cipher;

	CipherInfo(String sslVersion, CommonParser.NameList cipher) {
		this.sslVersion = sslVersionMap.getOrDefault(sslVersion, SSLVersion.UNKNOWN);
		this.cipher = new CipherSuiteSet();
		cipher.getList().forEach(e -> {
			CipherSuite c = PreservedCipherList.getFromName(e);
			if (c == null) {

				logger.warn("Cipher suite [" + e + "] not found from preserved list");
				c = new CipherSuite(-1, e, "", "", "-1", "");
			}

			logger.debug("Parsed cipher suite " + c);
			this.cipher.add(c);
		});
	}

	public SSLVersion getSslVersion() {
		return sslVersion;
	}

	public CipherSuiteSet getCipher() {
		return cipher;
	}

	public int compare(CipherInfo other) {
		return this.sslVersion.level - other.sslVersion.level;
	}

	@Override
	public String toString() {
		return "CipherInfo{" +
				"sslVersion=" + sslVersion.getLevel() +
				", cipher=" + cipher +
				'}';
	}
}
