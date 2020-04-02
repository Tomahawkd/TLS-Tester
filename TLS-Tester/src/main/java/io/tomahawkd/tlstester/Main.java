package io.tomahawkd.tlstester;

import com.beust.jcommander.ParameterException;
import de.rub.nds.tlsattacker.core.exceptions.TransportHandlerConnectException;
import io.tomahawkd.censys.exception.CensysException;
import io.tomahawkd.tlstester.analyzer.AnalyzerRunner;
import io.tomahawkd.tlstester.common.ComponentsLoader;
import io.tomahawkd.tlstester.config.ArgConfigImpl;
import io.tomahawkd.tlstester.config.ArgConfigurator;
import io.tomahawkd.tlstester.config.MiscArgDelegate;
import io.tomahawkd.tlstester.config.ScanningArgDelegate;
import io.tomahawkd.tlstester.data.DataCollectExecutor;
import io.tomahawkd.tlstester.data.DataHelper;
import io.tomahawkd.tlstester.data.TargetInfo;
import io.tomahawkd.tlstester.data.testssl.exception.FatalTagFoundException;
import io.tomahawkd.tlstester.database.RecorderHandler;
import io.tomahawkd.tlstester.netservice.CensysQueriesHelper;
import io.tomahawkd.tlstester.provider.TargetProvider;
import io.tomahawkd.tlstester.provider.sources.RuntimeSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.security.Security;
import java.util.Deque;
import java.util.concurrent.*;

public class Main {

	private static final Logger logger = LogManager.getLogger(Main.class);
	private static final String version = "v3.0.2";

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static void main(String[] args) {

		System.out.println(title);
		ArgConfigImpl config = new ArgConfigImpl();
		try {
			ArgConfigurator.INSTANCE.setConfig(config);
			config.parseArgs(args);

			if (config.isHelp()) return;
		} catch (ParameterException e) {
			return;
		} catch (IllegalArgumentException e) {
			// use Illegal Argument exception to get message print
			System.err.println(e.getMessage());
			if (config.getByType(MiscArgDelegate.class).isDebug()) {
				e.printStackTrace();
			}
			return;
		}

		// init procedure
		logger.info("Start initialize components.");
		ComponentsLoader.INSTANCE.loadExtensions();
		DataCollectExecutor.INSTANCE.init();
		AnalyzerRunner.INSTANCE.init();
		RecorderHandler.INSTANCE.init();
		logger.info("Components loaded.");

		try {
			logger.info("Activating testing procedure.");
			int threadCount = config.getByType(ScanningArgDelegate.class).getThreadCount();
			ThreadPoolExecutor executor =
					(ThreadPoolExecutor) Executors.newFixedThreadPool(
							threadCount <= 0 ? 1 : threadCount);
			Deque<Future<Void>> results = new ConcurrentLinkedDeque<>();

			TargetProvider provider = new TargetProvider(
					config.getByType(ScanningArgDelegate.class).getProviderSources());

			RuntimeSource censysSource = null;
			if (config.getByType(ScanningArgDelegate.class).checkOtherSiteCert()) {
				censysSource = new RuntimeSource();
			}

			// wait for main target
			run(executor, provider, results, censysSource);
			while (results.size() > 0) {
				results.pop().get();
			}
			logger.info("Host check complete.");

			// check host which has the same cert
			if (censysSource != null) {
				logger.info("Start checking host which use the same cert.");
				TargetProvider censysProvider = new TargetProvider(censysSource);
				run(executor, censysProvider, results, null);
				while (results.size() > 0) {
					results.pop().get();
				}
				logger.info("Host which use the same cert check complete.");
			}

			logger.info("Start updating result.");
			RecorderHandler.INSTANCE.getRecorder().postRecord();
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.SECONDS);
			executor.shutdownNow();
		} catch (Exception e) {
			logger.fatal("Unhandled Exception", e);
		} finally {
			RecorderHandler.INSTANCE.close();
			logger.info("Test complete, shutting down.");
		}
	}

	private static void run(ThreadPoolExecutor executor,
	                        TargetProvider provider,
	                        Deque<Future<Void>> results,
	                        RuntimeSource censysSource) {

		if (provider == null) return;

		provider.run();
		while (provider.hasMoreData()) {
			InetSocketAddress target = provider.getNextTarget();
			if (target == null) return;

			try {
				results.addLast(executor.submit(() -> {
					try {

						logger.info("Start testing host " + target);
						TargetInfo t = new TargetInfo(target);
						DataCollectExecutor.INSTANCE.collectInfoTo(t);
						if (DataHelper.isHasSSL(t) && censysSource != null) {
							try {
								censysSource.addAll(
										CensysQueriesHelper
												.searchIpWithHashSHA256(DataHelper.getCertHash(t)));
							} catch (CensysException e) {
								logger.error("Error on query censys", e);
							}
						}
						AnalyzerRunner.INSTANCE.analyze(t);
						logger.info("Testing complete, recording results");
						RecorderHandler.INSTANCE.getRecorder().record(t);

					} catch (FatalTagFoundException e) {
						logger.error("Fatal tag found in testssl result", e);
						logger.error("Fatal tag found in testssl result, Skip test host " + target);
					} catch (TransportHandlerConnectException e) {
						if (e.getCause() instanceof SocketTimeoutException)
							logger.error("Connecting to host {} timed out, skipping.",
									target, e);
						else logger.error(e);
					} catch (DataNotFoundException e) {
						logger.error("Testssl result not found, skipping");
					} catch (Exception e) {
						logger.error("Unhandled Exception, skipping", e);
					}
					return null;
				}));
			} catch (RejectedExecutionException e) {
				logger.error("Analysis to IP {} is rejected", target);
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
