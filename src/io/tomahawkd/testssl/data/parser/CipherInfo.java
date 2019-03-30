package io.tomahawkd.testssl.data.parser;

import java.util.LinkedHashMap;
import java.util.Map;

public class CipherInfo {

	public static final String splitSign = "~~~";

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
	private CommonParser.NameList cipher;

	CipherInfo(String sslVersion, CommonParser.NameList cipher) {
		this.sslVersion = sslVersionMap.getOrDefault(sslVersion, SSLVersion.UNKNOWN);
		this.cipher = cipher;
	}

	public SSLVersion getSslVersion() {
		return sslVersion;
	}

	public CommonParser.NameList getCipher() {
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
