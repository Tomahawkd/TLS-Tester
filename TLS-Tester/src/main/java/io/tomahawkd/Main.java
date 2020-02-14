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

	private static final String version = "v0.9";

	static {
		Security.addProvider(new BouncyCastleProvider());
		AnalyzerHelper.ignoreOtherCert();
	}

	public static void main(String[] args) {

		System.out.println(title);

		try {
			List<String> host = ShodanExplorer.explore("router", 10);
			host.addAll(ShodanExplorer.explore("netgear"));
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

					StatisticRecoder.addNonSSLRecord(s);
				}
			}
		} catch (Exception e) {
			logger.fatal("Unhandled Exception");
			e.printStackTrace();
		}
	}

	private static final String title =
			" ______  __       ____            ______                __                   \n" +
					"/\\__  _\\/\\ \\     /\\  _`\\         /\\__  _\\              /\\ \\__                \n" +
					"\\/_/\\ \\/\\ \\ \\    \\ \\,\\L\\_\\       \\/_/\\ \\/    __    ____\\ \\ ,_\\    __   _ __  \n" +
					"   \\ \\ \\ \\ \\ \\  __\\/_\\__ \\   _______\\ \\ \\  /'__`\\ /',__\\\\ \\ \\/  /'__`\\/\\`'__\\\n" +
					"    \\ \\ \\ \\ \\ \\L\\ \\ /\\ \\L\\ \\/\\______\\\\ \\ \\/\\  __//\\__, `\\\\ \\ \\_/\\  __/\\ \\ \\/ \n" +
					"     \\ \\_\\ \\ \\____/ \\ `\\____\\/______/ \\ \\_\\ \\____\\/\\____/ \\ \\__\\ \\____\\\\ \\_\\ \n" +
					"      \\/_/  \\/___/   \\/_____/          \\/_/\\/____/\\/___/   \\/__/\\/____/ \\/_/   " + version + "\n" +
					"                                                                             \n" +
					"A TLS channel security tester by Tomahawkd@Github\n" +
					"For more information please visit https://github.com/Tomahawkd/TLS-Tester\n" +
					"Thanks to http://patorjk.com/software/taag for Console ASCII art\n";
}
