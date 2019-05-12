package io.tomahawkd;

import de.rub.nds.tlsattacker.core.exceptions.TransportHandlerConnectException;
import io.tomahawkd.common.ShodanExplorer;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.detect.Analyzer;
import io.tomahawkd.detect.AnalyzerHelper;
import io.tomahawkd.detect.StatisticRecoder;
import io.tomahawkd.exception.NoSSLConnectionException;
import io.tomahawkd.testssl.ExecutionHelper;
import io.tomahawkd.testssl.data.TargetSegmentMap;
import io.tomahawkd.testssl.data.exception.FatalTagFoundException;
import io.tomahawkd.testssl.data.parser.CommonParser;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.net.SocketTimeoutException;
import java.security.Security;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);

	static {
		Security.addProvider(new BouncyCastleProvider());
		AnalyzerHelper.ignoreOtherCert();
	}

	public static void main(String[] args) {
		try {

			int threadCount = 4;
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);

			List<String> host = ShodanExplorer.explore("router", 10);
			host.addAll(ShodanExplorer.explore("netgear"));
			for (String s : host) {

				try {

					// task is too many
					while (executor.getActiveCount() >= threadCount) {
						Thread.sleep(10000);
					}

					executor.execute(() -> {
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
						} catch (Exception e) {
							logger.critical("Unhandled Exception, skipping");
							logger.critical(e.getMessage());
						}

					});
				} catch (RejectedExecutionException e) {
					logger.critical("Analysis to IP " + s + " is rejected");
				}
			}

			executor.shutdown();
		} catch (Exception e) {
			logger.fatal("Unhandled Exception");
			e.printStackTrace();
		}
	}
}
