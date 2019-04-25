package io.tomahawkd;

import de.rub.nds.tlsattacker.core.exceptions.TransportHandlerConnectException;
import io.tomahawkd.common.ShodanExplorer;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.Analyzer;
import io.tomahawkd.testssl.ExecutionHelper;
import io.tomahawkd.testssl.data.TargetSegmentMap;
import io.tomahawkd.testssl.data.parser.CipherInfo;
import io.tomahawkd.testssl.data.parser.CommonParser;
import io.tomahawkd.tlsattacker.ConnectionTester;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.net.SocketTimeoutException;
import java.security.Security;
import java.util.List;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static void main(String[] args) {
		try {
			List<String> host = ShodanExplorer.explore("has_ssl:true webcam");
			for (String s : host) {
				try {
					boolean isSSL = new ConnectionTester(s)
							.setNegotiateVersion(CipherInfo.SSLVersion.TLS1_2)
							.execute()
							.isServerHelloReceived();

					if (isSSL) {
						logger.info("Start testing host " + s);
						TargetSegmentMap t = CommonParser.parseFile(ExecutionHelper.runTest(s));
						t.forEach((ip, seg) -> Analyzer.analyze(seg));
					} else {
						logger.warn("host " + s + " do not have ssl connection, skipping.");
					}
				} catch (TransportHandlerConnectException e) {
					if (e.getCause() instanceof SocketTimeoutException)
						logger.critical("Connecting to host " + s + " timed out, skipping.");
					else logger.critical(e.getMessage());
				}
			}
		} catch (Exception e) {
			logger.fatal("Unhandled Exception");
			e.printStackTrace();
		}
	}
}
