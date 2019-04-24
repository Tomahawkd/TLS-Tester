package io.tomahawkd.common;

import io.tomahawkd.censys.AccountService;
import io.tomahawkd.censys.exception.CensysException;
import io.tomahawkd.censys.module.account.AccountMessage;
import io.tomahawkd.common.log.Logger;

import java.io.IOException;

public class CensysQueriesHelper {

	private static final Logger logger = Logger.getLogger(CensysQueriesHelper.class);

	private static final String path = "./temp/censys";
	private static final String extension = ".txt";

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

	public AccountService getServiceForApi() {
		return accountService;
	}
}
