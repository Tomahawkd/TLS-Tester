package io.tomahawkd;

import com.beust.jcommander.ParameterException;
import de.rub.nds.tlsattacker.core.exceptions.TransportHandlerConnectException;
import io.tomahawkd.analyzer.AnalyzerRunner;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.common.provider.TargetProvider;
import io.tomahawkd.data.TargetInfo;
import io.tomahawkd.testssl.data.exception.FatalTagFoundException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.net.SocketTimeoutException;
import java.security.Security;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

		try {

			int threadCount = ArgParser.INSTANCE.get().getThreadCount();
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);

//			ListTargetProvider<String> provider = new ListTargetProvider<>(ShodanExplorer.explore("has_ssl: true", 80));
//			TargetProvider<String> provider = FileTargetProvider.getDefault("./temp/test2.txt");
			List<TargetProvider<String>> providers = ArgParser.INSTANCE.get().getProviders();

			for (TargetProvider<String> provider : providers) {
				if (provider == null) {
					logger.fatal("Cannot parse a valid provider.");
					return;
				}

				while (provider.hasMoreData()) {
					String target = provider.getNextTarget();

					try {
						executor.execute(() -> {
							try {

								logger.info("Start testing host " + target);
								TargetInfo t = new TargetInfo(target);
								t.collectInfo();
								AnalyzerRunner analyzer = new AnalyzerRunner();
								analyzer.analyze(t);

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
							}
						});
					} catch (RejectedExecutionException e) {
						logger.critical("Analysis to IP " + target + " is rejected");
					}
				}
			}

			executor.shutdown();
			executor.awaitTermination(ArgParser.INSTANCE.get().getExecutionPoolTimeout(), TimeUnit.DAYS);
			//analyzer.postAnalyze();

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
