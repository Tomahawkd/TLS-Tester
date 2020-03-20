package io.tomahawkd.tlstester.database;

import io.tomahawkd.tlstester.database.delegate.BaseRecorderDelegate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Database(name = "mysql", authenticateRequired = true, useDriver = "com.mysql.cj.jdbc.Driver")
@TypeMap(string = "varchar(255)")
@SuppressWarnings("unused")
public class MysqlRecorderDelegate extends BaseRecorderDelegate {

	private static final Logger logger = LogManager.getLogger(MysqlRecorderDelegate.class);
	private String username;
	private String password;

	public MysqlRecorderDelegate(@Nullable String user, @Nullable String pass) {
		this.username = user;
		this.password = pass;
	}

	@Override
	public String getUrl(String dbname) {
		return "jdbc:mysql://localhost:3306/?useSSL=true&autoReconnect=true";
	}

	@Override
	public boolean checkTableExistence(String table, int type) throws SQLException {
		String sql;
		if (type == RecorderConstants.TABLE) {
			sql = "SELECT TABLE_NAME FROM information_schema.TABLES" +
					" WHERE TABLE_SCHEMA='" + getDbName() + "' " +
					"AND TABLE_NAME = '" + table + "';";
		} else if (type == RecorderConstants.VIEW) {
			sql = "SELECT TABLE_NAME FROM information_schema.VIEWS" +
					" WHERE TABLE_SCHEMA='" + getDbName() + "' " +
					"AND TABLE_NAME = '" + table + "';";
		} else throw new RuntimeException("Unknown table type " + type);

		ResultSet set = executeQuery(sql);
		boolean n = set.next();
		logger.debug(table + (n ? " " : " not ") + "exists.");
		return n;
	}

	@Override
	public boolean checkMissingColumns(String table, List<String> list)
			throws SQLException {
		String sql = "SELECT COLUMN_NAME FROM information_schema.COLUMNS " +
				"WHERE TABLE_SCHEMA = '" + getDbName() +
				"' AND TABLE_NAME = '" + table + "';";
		ResultSet s = executeQuery(sql);
		while (s.next()) {
			if (!list.contains(s.getString("COLUMN_NAME"))) {
				logger.debug("Column " + s.getString("COLUMN_NAME") +
						" in Table " + table + " not exists.");
				return true;
			}
			logger.debug("Column " + s.getString("COLUMN_NAME") +
					" in Table " + table + " exists.");
		}
		return false;
	}

	@Override
	public void preInit() throws SQLException {
		String schemaName = getDbName();
		String sql = "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA " +
				"WHERE SCHEMA_NAME = '" + schemaName + "';";
		ResultSet s = executeQuery(sql);

		// schema dont exist
		if (!s.next()) {
			executeUpdate("CREATE SCHEMA `" + schemaName + "`;");
		}
		executeUpdate("USE `" + schemaName + "`;");
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public String getPassword() {
		return this.password;
	}
}
