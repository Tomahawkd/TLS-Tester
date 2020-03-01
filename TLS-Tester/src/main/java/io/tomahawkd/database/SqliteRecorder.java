package io.tomahawkd.database;

import io.tomahawkd.common.log.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Database(name = "sqlite")
@TypeMap
@SuppressWarnings("unused")
public class SqliteRecorder extends AbstractRecorder {

	private static final Logger logger = Logger.getLogger(SqliteRecorder.class);

	@Override
	protected String getUrl(String dbname) {
		return "jdbc:sqlite:" + dbname + ".sqlite.db";
	}

	@Override
	protected boolean checkTableExistence(String table, int type) throws SQLException {

		String t;
		if (type == TABLE) t = "table";
		else if (type == VIEW) t = "view";
		else throw new RuntimeException("Unknown type " + type);

		String sql = "SELECT name FROM sqlite_master WHERE type='" + t + "' " +
				"AND name = '" + table + "';";
		Statement statement = this.connection.createStatement();
		ResultSet set = statement.executeQuery(sql);

		boolean n = set.next();
		logger.debug(t + " " + table + (n ? " " : " not ") + "exists.");
		return n;
	}

	protected boolean checkMissingColumns(String table, List<String> list)
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
