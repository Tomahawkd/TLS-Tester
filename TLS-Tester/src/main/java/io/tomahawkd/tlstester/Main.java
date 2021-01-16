package io.tomahawkd.tlstester;

import com.beust.jcommander.ParameterException;
import de.rub.nds.tlsattacker.core.exceptions.TransportHandlerConnectException;
import io.tomahawkd.config.ConfigManager;
import io.tomahawkd.config.commandline.CommandlineConfig;
import io.tomahawkd.config.commandline.CommandlineConfigSource;
import io.tomahawkd.config.sources.SourceManager;
import io.tomahawkd.tlstester.config.*;
import io.tomahawkd.tlstester.data.TargetInfo;
import io.tomahawkd.tlstester.data.testssl.exception.FatalTagFoundException;
import io.tomahawkd.tlstester.database.RecorderHandler;
import io.tomahawkd.tlstester.extensions.ExtensionManager;
import io.tomahawkd.tlstester.provider.TargetProvider;
import io.tomahawkd.tlstester.provider.TargetSourceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.net.SocketTimeoutException;
import java.security.Security;
import java.util.Deque;
import java.util.concurrent.*;

public class Main {

	private static final Logger logger = LogManager.getLogger(Main.class);
	private static final String version = "v3.2.2";

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static void main(String[] args) {

		System.out.println(title);
		ConfigManager configManager = ConfigManager.get();
		try {

			SourceManager.get().getSource(CommandlineConfigSource.class).setData(args);
			configManager.parse();

			if (configManager.getDelegateByType(HelpConfigDelegate.class).isHelp()) {
				System.out.println(configManager.getConfig(CommandlineConfig.class).usage());
				return;
			}
		} catch (ParameterException e) {
			System.out.println(configManager.getConfig(CommandlineConfig.class).usage());
			return;
		} catch (IllegalArgumentException | NullPointerException e) {
			// use Illegal Argument exception to get message print
			System.err.println(e.getMessage());
			if (configManager.getDelegateByType(MiscConfigDelegate.class).isDebug()) {
				e.printStackTrace();
			}
			return;
		}

		// init procedure
		logger.info("Start initialize components.");
		ExtensionManager.INSTANCE.loadComponents();
		logger.info("Components loaded.");

		try {
			logger.info("Activating testing procedure.");

			// testing configs
			int threadCount = configManager.getDelegateByType(ScanningConfigDelegate.class).getThreadCount();
			ThreadPoolExecutor executor =
					(ThreadPoolExecutor) Executors.newFixedThreadPool(
							threadCount <= 0 ? 1 : threadCount);
			Deque<Future<Void>> results = new ConcurrentLinkedDeque<>();

			TargetProvider provider = new TargetProvider(
					ExtensionManager.INSTANCE.get(TargetSourceFactory.class).build(
							configManager.getDelegateByType(ScanningConfigDelegate.class).getProviderSources()
					)
			);

			// wait for main target
			provider.run();
			while (provider.hasMoreData()) {
				TargetInfo target = provider.getNextTarget();
				if (target == null) continue;
				try {
					results.add(testProcedure(target, executor, provider));
				} catch (RejectedExecutionException e) {
					logger.error("Analysis to IP {} is rejected", target);
				}
			}
			while (results.size() > 0) {
				results.pop().get();
			}
			logger.info("Host check complete.");

			logger.info("Start updating result.");
			ExtensionManager.INSTANCE.get(RecorderHandler.class).getRecorder().postRecord();
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.SECONDS);
			executor.shutdownNow();
		} catch (Exception e) {
			logger.fatal("Unhandled Exception", e);
		} finally {
			ExtensionManager.INSTANCE.get(RecorderHandler.class).close();
			logger.info("Test complete, shutting down.");
		}
	}

	private static Future<Void> testProcedure(TargetInfo target,
	                                   ExecutorService executor,
	                                   TargetProvider provider) {
		return executor.submit(() -> {
			try {
				MainProcedure.run(target, provider);
			} catch (FatalTagFoundException e) {
				logger.error(
						"Fatal tag found in testssl result, Skip test host {}",
						target.getHost(), e);
			} catch (TransportHandlerConnectException e) {
				if (e.getCause() instanceof SocketTimeoutException)
					logger.error("Connecting to host {} timed out, skipping.",
							target.getHost(), e);
				else logger.error(e);
			} catch (DataNotFoundException e) {
				logger.error("Testssl result not found, skipping");
			} catch (Exception e) {
				logger.error("Unhandled Exception, skipping", e);
			}
			return null;
		});
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
