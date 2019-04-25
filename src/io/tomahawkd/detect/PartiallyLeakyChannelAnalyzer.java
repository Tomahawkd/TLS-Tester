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

	private StringBuilder resultText;

	public String getResult() {
		return resultText.toString();
	}

	public boolean checkVulnerability(SegmentMap target) {

		resultText = new StringBuilder();

		resultText.append("GOAL Partial decryption of messages sent by Client\n");
		resultText.append("-----------------------------------------------\n");
		resultText.append("| 1 CBC padding oracle on the server\n");

		boolean poodleTLS = isPoodleTlsVulnerable(target);
		boolean cbc = isCBCPaddingOracleVulnerable(target);

		boolean res = poodleTLS || cbc;

		if (res) logger.warn(resultText);
		else logger.ok(resultText);
		return res;
	}

	private boolean isPoodleTlsVulnerable(SegmentMap target) {

		resultText.append("\t| 1 POODLE-TLS padding oracle: ");
		boolean poodletls = new TLSPoodleTester().test(target.getIp());
		resultText.append(poodletls).append("\n");

		// use tls attacker instead
		resultText.append("\t\t& 1 Server checks TLS padding as in SSLv3\n");


		resultText.append("\t\t& 2 Any vulnerable CBC mode ciphersuite is used\n");


		resultText.append("\t\t\t| 1 A CBC mode ciphersuite is preferred " +
				"in the highest supported version of TLS");
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
		resultText.append(isPreferred).append("\n");


		resultText.append("\t\t\t| 2 Downgrade is possible to a version of " +
				"TLS where a CBC mode ciphersuite is preferred");
		boolean isPossible = AnalyzerHelper.downgradeIsPossibleToAVersionOf(target,
				CipherInfo.SSLVersion.TLS1,
				(version, suite, segmentMap) -> {
					if (suite.getName().contains("-CBC") || suite.getRfcName().contains("_CBC")) {
						return new ConnectionTester(segmentMap.getIp())
								.setCipherSuite(suite.getCipherForTesting())
								.setNegotiateVersion(version)
								.isServerHelloReceived();
					} else return false;
				});
		resultText.append(isPossible).append("\n");


		return poodletls;
	}

	private boolean isCBCPaddingOracleVulnerable(SegmentMap target) {

		resultText.append("\t| 2 CBC padding oracle - OpenSSL AES-NI bug");


		resultText.append("\t\t& 1 Server is vulnerable to CVE-2016-2107: ");
		boolean cve = new CveTester().test(target.getIp());
		resultText.append(cve).append("\n");


		resultText.append("\t\t& 2 A ciphersuite with AES in CBC mode is used\n");


		resultText.append("\t\t\t| 1 AES in CBC mode is preferred in the highest supported TLS version: ");
		CipherInfo max = AnalyzerHelper.getHighestSupportedCipherSuite(target);
		boolean isPreferred = false;
		for (CipherSuite suite : max.getCipher().getList()) {
			if ((suite.getName().contains("-CBC") || suite.getRfcName().contains("_CBC")) &&
					(suite.getName().contains("AES") || suite.getRfcName().contains("AES"))) {
				isPreferred = true;
				break;
			}
		}
		resultText.append(isPreferred).append("\n");


		resultText.append("\t\t\t|2 Downgrade is possible to a TLS version where AES in CBC mode is preferred: ");

		boolean isPossible = AnalyzerHelper.downgradeIsPossibleToAVersionOf(target,
				CipherInfo.SSLVersion.TLS1,
				((version, suite, segmentMap) -> {
					if ((suite.getName().contains("-CBC") || suite.getRfcName().contains("_CBC")) &&
							(suite.getName().contains("AES") || suite.getRfcName().contains("AES"))) {
						return new ConnectionTester(segmentMap.getIp())
								.setCipherSuite(suite.getCipherForTesting())
								.setNegotiateVersion(version)
								.isServerHelloReceived();
					} else return false;
				}));
		resultText.append(isPossible).append("\n");


		return cve && (isPreferred || isPossible);
	}
}
