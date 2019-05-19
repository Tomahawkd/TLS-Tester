package io.tomahawkd.detect;

import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.data.SegmentMap;
import io.tomahawkd.testssl.data.parser.CipherInfo;
import io.tomahawkd.testssl.data.parser.CipherSuite;
import io.tomahawkd.tlsattacker.ConnectionTester;
import io.tomahawkd.tlsattacker.CveTester;
import io.tomahawkd.tlsattacker.TLSPoodleTester;

public class PartiallyLeakyChannelAnalyzer {

	private static final Logger logger = Logger.getLogger(PartiallyLeakyChannelAnalyzer.class);

	public static final int CBC_PADDING = 0;
	public static final int POODLE = 1;
	public static final int VULNERABLE_CBC_USED = 2;
	public static final int CBC_CIPHER_PREFERRED = 3;
	public static final int CBC_CIPHER_DOWNGRADE = 4;
	public static final int OPENSSL_AES_NI = 5;
	public static final int CVE_2016_2107 = 6;
	public static final int AES_CBC_USED = 7;
	public static final int AES_CBC_PREFERRED = 8;
	public static final int AES_CBC_DOWNGRADE = 9;
	public static final int TREE_LENGTH = 10;

	private final TreeCode code = new TreeCode(TREE_LENGTH);

	public String getResult() {

		return "GOAL Partial decryption of messages sent by Client\n" +
				"-----------------------------------------------\n" +
				"| 1 CBC padding oracle on the server: " + code.get(CBC_PADDING) + "\n" +
				"\t| 1 POODLE-TLS padding oracle: " + code.get(POODLE) + "\n" +
				"\t\t& 1 Server checks TLS padding as in SSLv3\n" +
				"\t\t& 2 Any vulnerable CBC mode ciphersuite is used: " + code.get(VULNERABLE_CBC_USED) + "\n" +
				"\t\t\t| 1 A CBC mode ciphersuite is preferred " +
				"in the highest supported version of TLS: " +
				code.get(CBC_CIPHER_PREFERRED) + "\n" +
				"\t\t\t| 2 Downgrade is possible to a version of " +
				"TLS where a CBC mode ciphersuite is preferred: " +
				code.get(CBC_CIPHER_DOWNGRADE) + "\n" +
				"\t| 2 CBC padding oracle - OpenSSL AES-NI bug: " + code.get(OPENSSL_AES_NI) + "\n" +
				"\t\t& 1 Server is vulnerable to CVE-2016-2107: " + code.get(CVE_2016_2107) + "\n" +
				"\t\t& 2 A ciphersuite with AES in CBC mode is used: " + code.get(AES_CBC_USED) + "\n" +
				"\t\t\t| 1 AES in CBC mode is preferred in the highest supported TLS version: "
				+ code.get(AES_CBC_PREFERRED) + "\n" +
				"\t\t\t| 2 Downgrade is possible to a TLS version where AES in CBC mode is preferred: "
				+ code.get(AES_CBC_DOWNGRADE) + "\n";
	}

	public TreeCode getCode() {
		return code;
	}

	public boolean checkVulnerability(SegmentMap target) {

		boolean poodleTLS = isPoodleTlsVulnerable(target);
		code.set(poodleTLS, POODLE);

		boolean cbc = isCBCPaddingOracleVulnerable(target);
		code.set(cbc, OPENSSL_AES_NI);

		boolean res = poodleTLS || cbc;
		code.set(res, CBC_PADDING);

		String result = "\n" + getResult();
		if (res) logger.warn(result);
		else logger.ok(result);
		return res;
	}

	private boolean isPoodleTlsVulnerable(SegmentMap target) {

		boolean poodletls = new TLSPoodleTester().test(target.getIp());

		CipherInfo max = AnalyzerHelper.getHighestSupportedCipherSuite(target);
		if (max == null) {
			logger.fatal("No cipher got from segment");
			throw new IllegalArgumentException("No cipher got from segment");
		}

		boolean isPreferred = false;
		for (CipherSuite suite : max.getCipher().getList()) {
			if (suite.getName().contains("-CBC") || suite.getRfcName().contains("_CBC")) {
				isPreferred = true;
				break;
			}
		}
		code.set(isPreferred, CBC_CIPHER_PREFERRED);

		boolean isPossible = AnalyzerHelper.downgradeIsPossibleToAVersionOf(target,
				CipherInfo.SSLVersion.TLS1,
				(version, suite, segmentMap) -> {
					if (suite.getCipherForTesting() == null) {
						logger.critical("cipher isn't support by tls attacker, returning false");
						return false;
					}
					if (suite.getName().contains("-CBC") || suite.getRfcName().contains("_CBC")) {
						return new ConnectionTester(segmentMap.getIp())
								.setCipherSuite(suite.getCipherForTesting())
								.setNegotiateVersion(version)
								.execute()
								.isServerHelloReceived();
					} else return false;
				});
		code.set(isPossible, CBC_CIPHER_DOWNGRADE);

		boolean isUsed = isPreferred || isPossible;
		code.set(isUsed, VULNERABLE_CBC_USED);

		return poodletls;
	}

	private boolean isCBCPaddingOracleVulnerable(SegmentMap target) {

		boolean cve = new CveTester().test(target.getIp());
		code.set(cve, CVE_2016_2107);

		CipherInfo max = AnalyzerHelper.getHighestSupportedCipherSuite(target);
		boolean isPreferred = false;
		for (CipherSuite suite : max.getCipher().getList()) {
			if ((suite.getName().contains("-CBC") || suite.getRfcName().contains("_CBC")) &&
					(suite.getName().contains("AES") || suite.getRfcName().contains("AES"))) {
				isPreferred = true;
				break;
			}
		}
		code.set(isPreferred, AES_CBC_PREFERRED);

		boolean isPossible = AnalyzerHelper.downgradeIsPossibleToAVersionOf(target,
				CipherInfo.SSLVersion.TLS1,
				((version, suite, segmentMap) -> {
					if ((suite.getName().contains("-CBC") || suite.getRfcName().contains("_CBC")) &&
							(suite.getName().contains("AES") || suite.getRfcName().contains("AES"))) {

						if (suite.getCipherForTesting() == null) {
							logger.critical("cipher isn't support by tls attacker, returning false");
							return false;
						}
						return new ConnectionTester(segmentMap.getIp())
								.setCipherSuite(suite.getCipherForTesting())
								.setNegotiateVersion(version)
								.execute()
								.isServerHelloReceived();
					} else return false;
				}));
		code.set(isPossible, AES_CBC_DOWNGRADE);

		boolean isUsed = isPreferred || isPossible;
		code.set(isUsed, AES_CBC_USED);

		return cve && isUsed;
	}
}
