package io.tomahawkd;

import de.rub.nds.tlsattacker.core.exceptions.TransportHandlerConnectException;
import io.tomahawkd.common.FileHelper;
import io.tomahawkd.common.ShodanExplorer;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.common.provider.FileTargetProvider;
import io.tomahawkd.common.provider.ListTargetProvider;
import io.tomahawkd.common.provider.TargetProvider;
import io.tomahawkd.detect.Analyzer;
import io.tomahawkd.exception.NoSSLConnectionException;
import io.tomahawkd.testssl.ExecutionHelper;
import io.tomahawkd.testssl.data.TargetSegmentMap;
import io.tomahawkd.testssl.data.exception.FatalTagFoundException;
import io.tomahawkd.testssl.data.parser.CommonParser;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.Security;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);

	private static final String version = "v1.0";

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	private static void parseArgs(String[] args) {

		for (String arg : args) {
			if (arg.startsWith("--config=")) {
				try {
					Config.INSTANCE.loadFromFile(arg.split("=")[1]);
				} catch (IOException e) {
					logger.warn("Config load failed, use default");
				}

			}
		}
	}

	public static void main(String[] args) {

		System.out.println(title);
		parseArgs(args);

		try {

			int threadCount = Config.INSTANCE.get().getThreadCount();
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);

//			ListTargetProvider<String> provider = new ListTargetProvider<>(ShodanExplorer.explore("has_ssl: true", 80));
			TargetProvider<String> provider = FileTargetProvider.getDefault("./temp/test2.txt");

			while (provider.hasMoreData()) {

				String target = provider.getNextTarget();
				if (!target.contains(":")) target += ":443";

				try {
					String finalTarget = target;
					executor.execute(() -> {
						try {

							logger.info("Start testing host " + finalTarget);
							TargetSegmentMap t = CommonParser.parseFile(ExecutionHelper.runTest(finalTarget));
							t.forEach((ip, seg) -> Analyzer.analyze(seg));

						} catch (FatalTagFoundException e) {
							logger.critical(e.getMessage());
							logger.critical("Skip test host " + finalTarget);
						} catch (TransportHandlerConnectException e) {
							if (e.getCause() instanceof SocketTimeoutException)
								logger.critical("Connecting to host " + finalTarget + " timed out, skipping.");
							else logger.critical(e.getMessage());
						} catch (NoSSLConnectionException e) {
							logger.critical(e.getMessage());
							logger.critical("Skip test host " + finalTarget);

							Config.INSTANCE.getRecorder().addNonSSLRecord(finalTarget);
						} catch (Exception e) {
							logger.critical("Unhandled Exception, skipping");
							logger.critical(e.getMessage());
						}
					});
				} catch (RejectedExecutionException e) {
					logger.critical("Analysis to IP " + target + " is rejected");
				}
			}

			executor.shutdown();
			executor.awaitTermination(Config.INSTANCE.get().getExecutionPoolTimeout(), TimeUnit.DAYS);
			Analyzer.postAnalyze();

			Config.INSTANCE.printConfig();
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
