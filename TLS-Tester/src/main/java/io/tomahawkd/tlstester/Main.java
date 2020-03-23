package io.tomahawkd.tlstester;

import com.beust.jcommander.ParameterException;
import de.rub.nds.tlsattacker.core.exceptions.TransportHandlerConnectException;
import io.tomahawkd.censys.exception.CensysException;
import io.tomahawkd.tlstester.analyzer.AnalyzerRunner;
import io.tomahawkd.tlstester.common.ComponentsLoader;
import io.tomahawkd.tlstester.common.provider.ListTargetProvider;
import io.tomahawkd.tlstester.common.provider.TargetProvider;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.net.SocketTimeoutException;
import java.security.Security;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.*;

public class Main {

	private static final Logger logger = LogManager.getLogger(Main.class);
	private static final String version = "v2.3.4";

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
					(ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount <= 0 ? 1 : threadCount);
			Deque<Future<Void>> results = new ConcurrentLinkedDeque<>();

			List<TargetProvider<String>> providers =
					config.getByType(ScanningArgDelegate.class).getProviders();
			ListTargetProvider<String> censysProvider = null;
			if (config.getByType(ScanningArgDelegate.class).checkOtherSiteCert()) {
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
			logger.info("Test complete, shutting down.");
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
						if (DataHelper.isHasSSL(t) && censysProvider != null) {
							try {
								censysProvider.addAll(
										CensysQueriesHelper
												.searchIpWithHashSHA256(DataHelper.getCertHash(t)));
							} catch (CensysException e) {
								logger.error("Error on query censys");
								logger.error(e.getMessage());
							}
						}
						AnalyzerRunner.INSTANCE.analyze(t);
						logger.info("Testing complete, recording results");
						RecorderHandler.INSTANCE.getRecorder().record(t);

					} catch (FatalTagFoundException e) {
						logger.error(e.getMessage());
						logger.error("Skip test host " + target);
					} catch (TransportHandlerConnectException e) {
						if (e.getCause() instanceof SocketTimeoutException)
							logger.error("Connecting to host " + target +
									" timed out, skipping.");
						else logger.error(e.getMessage());
					} catch (Exception e) {
						logger.error("Unhandled Exception, skipping");
						logger.error(e.getMessage());
						e.printStackTrace();
					}
					return null;
				}));
			} catch (RejectedExecutionException e) {
				logger.error("Analysis to IP " + target + " is rejected");
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
