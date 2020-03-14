package io.tomahawkd.tlstester.database.delegate;

import io.tomahawkd.tlstester.common.log.Logger;
import io.tomahawkd.tlstester.database.RecorderConstants;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@SuppressWarnings("unused")
public class SqliteRecorderDelegate extends BaseRecorderDelegate {

	private static final Logger logger = Logger.getLogger(SqliteRecorderDelegate.class);

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
		ResultSet set = executeQuery(sql);

		boolean n = set.next();
		logger.debug(t + " " + table + (n ? " " : " not ") + "exists.");
		return n;
	}

	@Override
	public boolean checkMissingColumns(String table, List<String> list)
			throws SQLException {
		ResultSet s = executeQuery(
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
