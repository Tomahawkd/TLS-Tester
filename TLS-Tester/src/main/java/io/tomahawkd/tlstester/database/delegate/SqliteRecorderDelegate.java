package io.tomahawkd.tlstester.database.delegate;

import io.tomahawkd.tlstester.database.Database;
import io.tomahawkd.tlstester.database.RecorderConstants;
import io.tomahawkd.tlstester.database.TypeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Database(name = "sqlite", useDriver = "org.sqlite.JDBC")
@TypeMap
@SuppressWarnings("unused")
public class SqliteRecorderDelegate extends BaseRecorderDelegate {

	private static final Logger logger = LogManager.getLogger(SqliteRecorderDelegate.class);

	@Override
	public String getUrl() {
		return "jdbc:sqlite:" + getDbName() + ".sqlite.db";
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
