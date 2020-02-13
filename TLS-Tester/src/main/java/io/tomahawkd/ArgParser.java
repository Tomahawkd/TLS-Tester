package io.tomahawkd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public enum ArgParser {

	INSTANCE;

	private ArgItems items = new ArgItems();

	public void parseArgs(String[] args) {
		JCommander.newBuilder().addObject(items).build().parse(args);
	}

	public ArgItems get() {
		return items;
	}

	public static class ArgItems {

		@Parameter(names = {"--enable_cert"},
				description = "enable searching and testing other host has same cert. " +
						"It will be a long tour.")
		private boolean otherSiteCert = false;

		@Parameter(names = {"--thread_pool_timeout"}, description = "Thread pool execution day. " +
				"The program will terminate if the time expires.")
		private Integer executionPoolTimeout = 1;

		@Parameter(names = {"-t", "--thread"}, description = "Total thread to be activated.")
		private Integer threadCount = 5;

		@Parameter(names = "--testssl", description = "Testssl path. " +
				"(No slash at the end, default is ./testssl.sh)")
		private String testsslPath = "./testssl.sh";

		@Parameter(names = "--temp", description = "Temp file expired time.")
		private int tempExpireTime = 7;

		@Parameter(names = "--db", description = "Database name. default: data.sqlite.db")
		private String dbName = "data.sqlite.db";

		@Parameter(names = { "-h", "-help" }, help = true,
				description = "Prints usage for all the existing commands.")
		private boolean help;

		public boolean checkOtherSiteCert() {
			return otherSiteCert;
		}

		public Integer getExecutionPoolTimeout() {
			return executionPoolTimeout;
		}

		public Integer getThreadCount() {
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
