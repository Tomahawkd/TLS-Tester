package io.tomahawkd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import io.tomahawkd.common.log.LogLevel;
import io.tomahawkd.common.log.LoggerManager;
import io.tomahawkd.common.provider.TargetProvider;
import io.tomahawkd.common.provider.TargetProviderDelegate;

import java.util.ArrayList;
import java.util.List;

public enum ArgParser {

	INSTANCE;

	private ArgItems items = new ArgItems();

	public void parseArgs(String[] args) {
		JCommander c = JCommander.newBuilder().addObject(items).build();
		try {
			c.parse(args);

			for (String s : items.providersList)
				items.providers.add(TargetProviderDelegate.convert(s));
		} catch (ParameterException e) {
			System.err.println(e.getMessage());
			c.usage();
			throw e;
		}
	}

	public ArgItems get() {
		return items;
	}

	public static class ArgItems {

		@Parameter(required = true,
				description = "<Type>::<Target String>\n" +
						"Available options:\n" +
						"shodan[::<start>-<end>]::<query>\n" +
						"file::<path>\n" +
						"ips::<ip>[;<ip>]")
		@SuppressWarnings("all")
		private List<String> providersList = new ArrayList<>();
		private List<TargetProvider<String>> providers = new ArrayList<>();

		@Parameter(names = {"--enable_cert"},
				description = "enable searching and testing other host has same cert. " +
						"It will be a long tour.")
		private boolean otherSiteCert = false;

		@Parameter(names = {"--thread_pool_timeout"}, description = "Thread pool execution day. " +
				"The program will terminate if the time expires.")
		private Integer executionPoolTimeout = 1;

		@Parameter(names = {"-t", "--thread"}, description = "Total thread to be activated.")
		private Integer threadCount = 5;

		@Parameter(names = "--testssl", description = "Testssl path. (No slash at the end)")
		private String testsslPath = "./testssl.sh";

		@Parameter(names = "--temp", description = "Temp file expired day. (-1 indicates forever)")
		private Integer tempExpireTime = 7;

		@Parameter(names = "--db", description = "Database name.")
		private String dbName = "data.sqlite.db";

		@Parameter(names = "--log", description = "Override default logging level.",
				validateWith = LoggerManager.class)
		@SuppressWarnings("unused")
		private Integer logLevel = LogLevel.OK.getLevel();

		@Parameter(names = {"-h", "-help"}, help = true,
				description = "Prints usage for all the existing commands.")
		@SuppressWarnings("unused")
		private boolean help = false;

		public List<TargetProvider<String>> getProviders() {
			return providers;
		}

		public boolean checkOtherSiteCert() {
			return otherSiteCert;
		}

		public int getExecutionPoolTimeout() {
			return executionPoolTimeout;
		}

		public int getThreadCount() {
			return threadCount;
		}

		public String getTestsslPath() {
			return testsslPath;
		}

		public int getTempExpireTime() {
			return tempExpireTime;
		}

		public String getDbName() {
			return dbName;
		}
	}
}
