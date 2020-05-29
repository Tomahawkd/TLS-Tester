package io.tomahawkd.tlstester.netservice;

import io.tomahawkd.censys.AccountService;
import io.tomahawkd.censys.IpSearchApi;
import io.tomahawkd.censys.exception.CensysException;
import io.tomahawkd.censys.module.account.AccountMessage;
import io.tomahawkd.censys.module.searching.IpSearchMessage;
import io.tomahawkd.tlstester.common.FileHelper;
import io.tomahawkd.tlstester.data.Callback;
import io.tomahawkd.tlstester.data.DataHelper;
import io.tomahawkd.tlstester.data.TargetInfoFactory;
import io.tomahawkd.tlstester.data.testssl.parser.CommonParser;
import io.tomahawkd.tlstester.provider.sources.SourcesStreamHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CensysQueriesHelper {

	private static final Logger logger = LogManager.getLogger(CensysQueriesHelper.class);

	private static final String path = FileHelper.TEMP + "/censys/";
	private static final String extension = ".txt";

	static {
		try {
			if (!FileHelper.isDirExist(path)) FileHelper.createDir(path);
		} catch (IOException e) {
			throw new RuntimeException("Could not create censys directory");
		}
	}

	private static AccountService accountService;

	static {
		try {
			String[] content = FileHelper.readFile("./keys/censys_key").split("\n");
			accountService = AccountService.acquireToken(content[0], content[1]);
		} catch (IOException e) {
			logger.fatal("Error on loading api file");
		} catch (IndexOutOfBoundsException e) {
			String message = "Invalid api file";
			logger.fatal(message);
			throw new IllegalStateException(message);
		}
	}

	public static synchronized void checkCredits() {
		try {
			AccountMessage accountMessage = accountService.status();

			int amount = accountMessage.getQuotaAllowance();
			int used = accountMessage.getUsedQuotaCount();
			logger.info("Censys credit remaining: {}", amount - used);
			if (amount - used < 0) {
				logger.fatal("No more query");
				throw new CensysException("No more query");
			}
		} catch (IndexOutOfBoundsException e) {
			String message = "Invalid api file";
			logger.fatal(message);
			throw new IllegalStateException(message);
		} catch (CensysException e) {
			logger.fatal("Error while retrieving user information", e);
			throw e;
		}

		// 0.4/s
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ignored) {
		}
	}

	public static synchronized List<String> searchIpWithHashSHA256(String hash) throws Exception {

		if (hash.trim().isEmpty()) return new ArrayList<>();

		String file = path + hash + extension;
		logger.debug("IP file: " + file);

		checkCredits();
		String data = FileHelper.Cache.getContentIfValidOrDefault(file, () -> {
			IpSearchMessage response = new IpSearchApi(accountService)
					.search(hash.toUpperCase(), 1, null);

			StringBuilder builder = new StringBuilder();
			response.getResults().forEach(message -> {
				String host = message.getIp();
				message.getProtocols().forEach(port ->
						builder.append(host).append(":")
								.append(port.getPort()).append("\n"));
			});

			return builder.toString();
		});

		return CommonParser.parseHost(data);
	}

	public static final Callback CENSYS_CALLBACK = (info, storage) -> {
		try {
			SourcesStreamHelper.process(
					searchIpWithHashSHA256(DataHelper.getCertHash(info)).stream()
			)
					.map(TargetInfoFactory::defaultBuild)
					.forEach(storage::add);
		} catch (CensysException e) {
			logger.error("Error on query censys", e);
		} catch (Exception e) {
			logger.error("Unexpect exception", e);
		}
	};
}
