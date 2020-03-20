package io.tomahawkd.tlstester.analyzer;

import io.tomahawkd.tlstester.data.DataHelper;
import io.tomahawkd.tlstester.data.TargetInfo;
import io.tomahawkd.tlstester.data.TreeCode;
import io.tomahawkd.tlstester.data.testssl.SegmentMap;
import io.tomahawkd.tlstester.data.testssl.parser.CipherInfo;
import io.tomahawkd.tlstester.data.testssl.parser.CipherSuite;
import io.tomahawkd.tlstester.tlsattacker.ConnectionTester;
import io.tomahawkd.tlstester.tlsattacker.PaddingOracleTester;
import io.tomahawkd.tlstester.tlsattacker.TLSPoodleTester;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Record(column = "partially_leaky", resultLength = PartiallyLeakyChannelAnalyzer.TREE_LENGTH,
		map = {
				@StatisticMapping(column = "overall", position = PartiallyLeakyChannelAnalyzer.CBC_PADDING),
				@StatisticMapping(column = "poodle", position = PartiallyLeakyChannelAnalyzer.POODLE),
				@StatisticMapping(column = "aes_ni", position = PartiallyLeakyChannelAnalyzer.OPENSSL_AES_NI)
		})
@SuppressWarnings("unused")
public class PartiallyLeakyChannelAnalyzer implements Analyzer {

	private static final Logger logger = LogManager.getLogger(PartiallyLeakyChannelAnalyzer.class);

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

	@Override
	public boolean getResult(TreeCode code) {
		return code.get(CBC_PADDING);
	}

	@Override
	public String getResultDescription(TreeCode code) {

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

	public void analyze(TargetInfo info, TreeCode code) {

		logger.info("Start test partially leaky channel on " + info.getHost());

		boolean poodleTLS = isPoodleTlsVulnerable(DataHelper.getTargetData(info), code);
		code.set(poodleTLS, POODLE);

		boolean cbc = isCBCPaddingOracleVulnerable(DataHelper.getTargetData(info), code);
		code.set(cbc, OPENSSL_AES_NI);

		boolean res = poodleTLS || cbc;
		code.set(res, CBC_PADDING);
	}

	@Override
	public void postAnalyze(TargetInfo info, TreeCode code) {
		logger.debug("Result: " + code);
		String result = "\n" + getResultDescription(code);
		if (getResult(code)) logger.warn(result);
		else logger.info(result);
	}

	private boolean isPoodleTlsVulnerable(SegmentMap target, TreeCode code) {

		boolean poodletls = new TLSPoodleTester().test(target.getIp());

		CipherInfo max = AnalyzerHelper.getHighestSupportedCipherSuite(target);
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
					if (de.rub.nds.tlsattacker.core.constants.
							CipherSuite.getCipherSuite(suite.getHexCode()) == null) {
						logger.error("cipher isn't support by tls attacker, returning false");
						return false;
					}
					if (suite.getName().contains("-CBC") || suite.getRfcName().contains("_CBC")) {
						return new ConnectionTester(segmentMap.getIp())
								.setCipherSuite(de.rub.nds.tlsattacker.core.constants.
										CipherSuite.getCipherSuite(suite.getHexCode()))
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

	private boolean isCBCPaddingOracleVulnerable(SegmentMap target, TreeCode code) {

		boolean cve = new PaddingOracleTester().test(target.getIp());
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

						if (de.rub.nds.tlsattacker.core.constants.
								CipherSuite.getCipherSuite(suite.getHexCode()) == null) {
							logger.error("cipher isn't support by tls attacker, returning false");
							return false;
						}
						return new ConnectionTester(segmentMap.getIp())
								.setCipherSuite(de.rub.nds.tlsattacker.core.constants.
										CipherSuite.getCipherSuite(suite.getHexCode()))
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
