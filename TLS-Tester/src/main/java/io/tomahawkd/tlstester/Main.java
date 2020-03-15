package io.tomahawkd.tlstester;

import com.beust.jcommander.ParameterException;
import de.rub.nds.tlsattacker.core.exceptions.TransportHandlerConnectException;
import io.tomahawkd.tlstester.analyzer.AnalyzerRunner;
import io.tomahawkd.censys.exception.CensysException;
import io.tomahawkd.tlstester.common.ComponentsLoader;
import io.tomahawkd.tlstester.common.log.Logger;
import io.tomahawkd.tlstester.common.provider.ListTargetProvider;
import io.tomahawkd.tlstester.common.provider.TargetProvider;
import io.tomahawkd.tlstester.data.DataCollectExecutor;
import io.tomahawkd.tlstester.data.TargetInfo;
import io.tomahawkd.tlstester.database.RecorderHandler;
import io.tomahawkd.tlstester.netservice.CensysQueriesHelper;
import io.tomahawkd.tlstester.data.testssl.exception.FatalTagFoundException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.net.SocketTimeoutException;
import java.security.Security;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.*;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);
	private static final String version = "v2.0";

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static void main(String[] args) {

		System.out.println(title);
		try {
			ArgParser.INSTANCE.parseArgs(args);
		} catch (ParameterException e) {
			return;
		}

		// init procedure
		logger.debug("Start initialize components");
		ComponentsLoader.INSTANCE.loadExtensions();
		DataCollectExecutor.INSTANCE.init();
		AnalyzerRunner.INSTANCE.init();
		RecorderHandler.INSTANCE.init();
		logger.debug("Components loaded.");

		try {
			logger.debug("Activating testing procedure.");
			int threadCount = ArgParser.INSTANCE.get().getThreadCount();
			ThreadPoolExecutor executor =
					(ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount <= 0 ? 1 : threadCount);
			Deque<Future<Void>> results = new ConcurrentLinkedDeque<>();

			List<TargetProvider<String>> providers = ArgParser.INSTANCE.get().getProviders();
			ListTargetProvider<String> censysProvider = null;
			if (ArgParser.INSTANCE.get().checkOtherSiteCert()) {
				censysProvider = new ListTargetProvider<>();
			}

			// wait for main target
			for (TargetProvider<String> provider : providers) {
				if (provider == null) {
					logger.fatal("Cannot parse a valid provider.");
					continue;
				}
				run(executor, provider, results, censysProvider);
			}
			while (results.size() > 0) {
				results.pop().get();
			}

			logger.info("Host check complete.");
			// check host which has the same cert
			if (censysProvider != null) {
				logger.info("Start checking host which use the same cert.");
				censysProvider.setFinish();
				run(executor, censysProvider, results, null);
				while (results.size() > 0) {
					results.pop().get();
				}
				logger.info("Host which use the same cert check complete.");
			}

			logger.info("Start updating result.");
			RecorderHandler.INSTANCE.getRecorder().postRecord();
			executor.shutdownNow();
			executor.awaitTermination(1, TimeUnit.SECONDS);
		} catch (Exception e) {
			logger.fatal("Unhandled Exception");
			e.printStackTrace();
		} finally {
			RecorderHandler.INSTANCE.close();
			logger.ok("Test complete, shutting down.");
		}
	}

	private static void run(ThreadPoolExecutor executor,
	                        TargetProvider<String> provider,
	                        Deque<Future<Void>> results,
	                        TargetProvider<String> censysProvider) {

		if (provider == null) return;

		while (provider.hasMoreData()) {
			String target = provider.getNextTarget();

			try {
				results.push(executor.submit(() -> {
					try {

						logger.info("Start testing host " + target);
						TargetInfo t = new TargetInfo(target);
						DataCollectExecutor.INSTANCE.collectInfoTo(t);
						if (t.isHasSSL() && censysProvider != null) {
							try {
								censysProvider.addAll(
										CensysQueriesHelper
												.searchIpWithHashSHA256(t.getCertHash()));
							} catch (CensysException e) {
								logger.critical("Error on query censys");
								logger.critical(e.getMessage());
							}
						}
						AnalyzerRunner.INSTANCE.analyze(t);
						logger.info("Testing complete, recording results");
						RecorderHandler.INSTANCE.getRecorder().record(t);

					} catch (FatalTagFoundException e) {
						logger.critical(e.getMessage());
						logger.critical("Skip test host " + target);
					} catch (TransportHandlerConnectException e) {
						if (e.getCause() instanceof SocketTimeoutException)
							logger.critical("Connecting to host " + target +
									" timed out, skipping.");
						else logger.critical(e.getMessage());
					} catch (Exception e) {
						logger.critical("Unhandled Exception, skipping");
						logger.critical(e.getMessage());
						e.printStackTrace();
					}
					return null;
				}));
			} catch (RejectedExecutionException e) {
				logger.critical("Analysis to IP " + target + " is rejected");
			}
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
