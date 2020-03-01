package io.tomahawkd.database.sample;

import io.tomahawkd.ArgParser;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.database.AbstractRecorder;
import io.tomahawkd.database.Database;
import io.tomahawkd.database.TypeMap;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Database(name = "mysql", authenticateRequired = true)
@TypeMap(string = "varchar(255)", integer = "BIGINT")
@SuppressWarnings("unused")
public class MysqlRecorder extends AbstractRecorder {

	private static final Logger logger = Logger.getLogger(MysqlRecorder.class);

	@Override
	protected String getUrl(String dbname) {
		return "jdbc:mysql://localhost:3306/?useSSL=true&autoReconnect=true";
	}

	@Override
	protected boolean checkTableExistence(String table, int type) throws SQLException {
		String sql;
		if (type == TABLE) {
			sql = "SELECT TABLE_NAME FROM information_schema.TABLES" +
					" WHERE TABLE_SCHEMA='" + ArgParser.INSTANCE.get().getDbName() + "' " +
					"AND TABLE_NAME = '" + table + "';";
		} else if (type == VIEW) {
			sql = "SELECT TABLE_NAME FROM information_schema.VIEWS" +
					" WHERE TABLE_SCHEMA='" + ArgParser.INSTANCE.get().getDbName() + "' " +
					"AND TABLE_NAME = '" + table + "';";
		} else throw new RuntimeException("Unknown table type " + type);

		Statement statement = this.connection.createStatement();
		ResultSet set = statement.executeQuery(sql);

		boolean n = set.next();
		logger.debug(table + (n ? " " : " not ") + "exists.");
		return n;
	}

	@Override
	protected boolean checkMissingColumns(String table, List<String> list)
			throws SQLException {
		String sql = "SELECT COLUMN_NAME FROM information_schema.COLUMNS " +
				"WHERE TABLE_SCHEMA = '" + ArgParser.INSTANCE.get().getDbName() +
				"' AND TABLE_NAME = '" + table + "';";
		ResultSet s = this.connection.createStatement().executeQuery(sql);
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
	protected void init() throws SQLException {

		String schemaName = ArgParser.INSTANCE.get().getDbName();
		String sql = "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA " +
				"WHERE SCHEMA_NAME = '" + schemaName + "';";
		ResultSet s = this.connection.createStatement().executeQuery(sql);

		// schema dont exist
		if (!s.next()) {
			this.connection.createStatement().executeUpdate("CREATE SCHEMA `" + schemaName + "`;");
		}
		this.connection.createStatement().executeUpdate("USE `" + schemaName + "`;");
		super.init();
	}

	public MysqlRecorder(@Nullable String user, @Nullable String pass) {
		super(user, pass);
	}
}
