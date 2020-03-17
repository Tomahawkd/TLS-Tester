package io.tomahawkd.tlstester.config;

import com.beust.jcommander.Parameter;

public class DatabaseArgDelegate extends AbstractArgDelegate {

	@Parameter(names = "--db", description = "Database name.")
	private String dbName = "tlstester";

	@Parameter(names = "--db_type", description = "Database type(sqlite etc.).")
	private String dbType = "sqlite";

	@Parameter(names = "--db_user", description = "Database username (if any).")
	private String dbUser = "";

	@Parameter(names = "--db_pass", description = "Database password (if any).")
	private String dbPass = "";

	public String getDbName() {
		return dbName;
	}

	public String getDbType() {
		return dbType;
	}

	public String getDbUser() {
		return dbUser;
	}

	public String getDbPass() {
		return dbPass;
	}
}
