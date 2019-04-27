package io.tomahawkd.common;

import io.tomahawkd.censys.AccountService;
import io.tomahawkd.censys.IpSearchApi;
import io.tomahawkd.censys.exception.CensysException;
import io.tomahawkd.censys.module.account.AccountMessage;
import io.tomahawkd.censys.module.searching.IpSearchMessage;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.data.parser.CommonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CensysQueriesHelper {

	private static final Logger logger = Logger.getLogger(CensysQueriesHelper.class);

	private static final String path = "./temp/censys/";
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
			String[] content = FileHelper.readFile("./temp/censys").split("\n");
			accountService = AccountService.acquireToken(content[0], content[1]);
			AccountMessage accountMessage = accountService.status();

			int amount = accountMessage.getQuotaAllowance();
			logger.info("You are allowed to query " + amount);
			int used = accountMessage.getUsedQuotaCount();
			logger.info("You used " + used);
			if (amount - used < 0) {
				logger.fatal("No more query");
				throw new IllegalArgumentException("No more query");
			}

		} catch (IOException e) {
			logger.fatal("Error on loading api file");
		} catch (IndexOutOfBoundsException e) {
			String message = "Invalid api file";
			logger.fatal(message);
			throw new IllegalStateException(message);
		} catch (CensysException e){
			logger.fatal("Error while retrieving user information");
			logger.fatal(e.getMessage());
			throw new IllegalStateException(e.getMessage());
		}
	}

	public static List<String> searchIpWithHashSHA256(String hash) throws Exception {

		if (hash.trim().isEmpty()) return new ArrayList<>();

		String file = path + hash + extension;
		logger.debug("IP file: " + file);

		String data = FileHelper.Cache.getContentIfValidOrDefault(file, () -> {
			IpSearchMessage response = new IpSearchApi(accountService)
					.search(hash.toUpperCase(), 1, null);

			StringBuilder builder = new StringBuilder();
			response.getResults().forEach(message -> {
				builder.append(message.getIp()).append("\n");
			});

			return builder.toString();
		});

		return CommonParser.parseHost(data);
	}
}
