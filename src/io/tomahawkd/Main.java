package io.tomahawkd;

import de.rub.nds.tlsattacker.core.exceptions.TransportHandlerConnectException;
import io.tomahawkd.common.ShodanExplorer;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.detect.AnalyzerHelper;
import io.tomahawkd.detect.StatisticRecoder;
import io.tomahawkd.exception.NoSSLConnectionException;
import io.tomahawkd.detect.Analyzer;
import io.tomahawkd.testssl.ExecutionHelper;
import io.tomahawkd.testssl.data.TargetSegmentMap;
import io.tomahawkd.testssl.data.exception.FatalTagFoundException;
import io.tomahawkd.testssl.data.parser.CommonParser;
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

					logger.info("Start testing host " + s);
					TargetSegmentMap t = CommonParser.parseFile(ExecutionHelper.runTest(s));
					t.forEach((ip, seg) -> Analyzer.analyze(seg));

				} catch (FatalTagFoundException e) {
					logger.critical(e.getMessage());
					logger.critical("Skip test host " + s);
				} catch (TransportHandlerConnectException e) {
					if (e.getCause() instanceof SocketTimeoutException)
						logger.critical("Connecting to host " + s + " timed out, skipping.");
					else logger.critical(e.getMessage());
				} catch (NoSSLConnectionException e) {
					logger.critical(e.getMessage());
					logger.critical("Skip test host " + s);

					StatisticRecoder.addRecord(s, false, false, false, false);
				}
			}
		} catch (Exception e) {
			logger.fatal("Unhandled Exception");
			e.printStackTrace();
		}
	}
}
