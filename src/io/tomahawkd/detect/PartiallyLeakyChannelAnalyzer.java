package io.tomahawkd.detect;

import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.data.SegmentMap;
import io.tomahawkd.testssl.data.parser.CipherInfo;
import io.tomahawkd.testssl.data.parser.CipherSuite;
import io.tomahawkd.tlsattacker.CveTester;
import io.tomahawkd.tlsattacker.DowngradeTester;

import java.util.List;

public class PartiallyLeakyChannelAnalyzer {

	private static final Logger logger = Logger.getLogger(PartiallyLeakyChannelAnalyzer.class);

	private static StringBuilder resultText;

	public static boolean checkVulnerability(SegmentMap target) {

		resultText = new StringBuilder();

		resultText.append("Checking ").append(target.getIp()).append("\n\n");
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

	private static boolean isPoodleTlsVulnerable(SegmentMap target) {

		resultText.append("\t| 1 POODLE-TLS padding oracle\n");


		resultText.append("\t\t& 1 Server checks TLS padding as in SSLv3: ");
		boolean isPadding = false;

		resultText.append(isPadding).append("\n");


		resultText.append("\t\t& 2 Any vulnerable CBC mode ciphersuite is used\n");


		resultText.append("\t\t\t| 1 A CBC mode ciphersuite is preferred " +
				"in the highest supported version of TLS");
		CipherInfo max = AnalyzerHelper.getHighestSupportedCipherSuite(target);
		if (max == null) {
			logger.fatal("No cipher got from segment");
			throw new IllegalArgumentException("No cipher got from segment");
		}

		boolean isPreferred = false;
		CipherSuite targetCipher = null;
		for (CipherSuite suite : max.getCipher().getList()) {
			if (suite.getName().contains("-CBC") || suite.getRfcName().contains("_CBC")) {
				isPreferred = true;
				targetCipher = suite;
				break;
			}
		}
		resultText.append(isPreferred).append("\n");


		resultText.append("\t\t\t| 2 Downgrade is possible to a version of " +
				"TLS where a CBC mode ciphersuite is preferred");
		boolean isPossible = false;
		// todo: if target is null you need to set cipher manually
		if (isPreferred) {
			List<HandshakeMessage> list =
					new DowngradeTester(target.getIp())
							.setCipherSuite(targetCipher.getCipherForTesting())
							.execute();

			if (list.size() > 1) isPossible = true;
		}
		resultText.append(isPossible).append("\n");


		return isPadding && (isPreferred || isPossible);
	}

	private static boolean isCBCPaddingOracleVulnerable(SegmentMap target) {

		resultText.append("\t| 2 CBC padding oracle - OpenSSL AES-NI bug");


		resultText.append("\t\t& 1 Server is vulnerable to CVE-2016-2107: ");
		boolean cve = CveTester.test(target.getIp());
		resultText.append(cve).append("\n");


		resultText.append("\t\t& 2 A ciphersuite with AES in CBC mode is used\n");


		resultText.append("\t\t\t| 1 AES in CBC mode is preferred in the highest supported TLS version: ");
		CipherInfo max = AnalyzerHelper.getHighestSupportedCipherSuite(target);
		CipherSuite targetCipher = null;
		boolean isPreferred = false;
		for (CipherSuite suite : max.getCipher().getList()) {
			if ((suite.getName().contains("-CBC") || suite.getRfcName().contains("_CBC")) &&
					(suite.getName().contains("AES") || suite.getRfcName().contains("AES"))) {
				isPreferred = true;
				targetCipher = suite;
				break;
			}
		}
		resultText.append(isPreferred).append("\n");


		resultText.append("\t\t\t|2 Downgrade is possible to a TLS version where AES in CBC mode is preferred: ");
		boolean isPossible = false;
		// todo: if target is null you need to set cipher manually
		if (targetCipher != null) {
			List<HandshakeMessage> list =
					new DowngradeTester(target.getIp())
							.setCipherSuite(targetCipher.getCipherForTesting())
							.execute();

			if (list.size() > 1) isPossible = true;
		}
		resultText.append(isPossible).append("\n");


		return cve && (isPreferred || isPossible);
	}
}
