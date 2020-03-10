package io.tomahawkd.database.delegate;

import io.tomahawkd.common.log.Logger;
import io.tomahawkd.database.Database;
import io.tomahawkd.database.RecorderConstants;
import io.tomahawkd.database.TypeMap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Database(name = "sqlite")
@TypeMap
@SuppressWarnings("unused")
public class SqliteRecorderDelegate extends AbstractRecorderDelegate {

	private static final Logger logger = Logger.getLogger(SqliteRecorderDelegate.class);
	private Connection connection;

	@Override
	public String getUrl(String dbname) {
		return "jdbc:sqlite:" + dbname + ".sqlite.db";
	}

	@Override
	public boolean checkTableExistence(String table, int type) throws SQLException {

		String t;
		if (type == RecorderConstants.TABLE) t = "table";
		else if (type == RecorderConstants.VIEW) t = "view";
		else throw new RuntimeException("Unknown type " + type);

		String sql = "SELECT name FROM sqlite_master WHERE type='" + t + "' " +
				"AND name = '" + table + "';";
		Statement statement = this.connection.createStatement();
		ResultSet set = statement.executeQuery(sql);

		boolean n = set.next();
		logger.debug(t + " " + table + (n ? " " : " not ") + "exists.");
		return n;
	}

	@Override
	public boolean checkMissingColumns(String table, List<String> list)
			throws SQLException {
		ResultSet s = this.connection.createStatement().executeQuery(
				"PRAGMA table_info(" + table + ");");
		while (s.next()) {
			if (!list.contains(s.getString("name"))) {
				logger.debug("Column " + s.getString("name") +
						" in Table " + table + " not exists.");
				return true;
			}
			logger.debug("Column " + s.getString("name") +
					" in Table " + table + " exists.");
		}
		return false;
	}
}
