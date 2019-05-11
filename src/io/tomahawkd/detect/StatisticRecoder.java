package io.tomahawkd.detect;

import io.tomahawkd.common.log.Logger;
import io.tomahawkd.identifier.CommonIdentifier;
import io.tomahawkd.identifier.IdentifierHelper;

import java.sql.*;

public class StatisticRecoder {

	private static final String sqlitePath = "./statistic.sqlite.db";
	private static final String table = "statistic";

	private static final Logger logger = Logger.getLogger(StatisticRecoder.class);

	private static Connection connection = null;

	static {

		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);

			String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name = '" + table + "';";
			Statement statement = connection.createStatement();
			ResultSet set = statement.executeQuery(sql);

			if (set.getFetchSize() < 1) {
				Statement stat = connection.createStatement();

				stat.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + " ("
						+ "	ip text PRIMARY KEY,"
						+ "identifier text not null, "
						+ "	ssl_enabled boolean default false,"
						+ "	leaky boolean default false,"
						+ "tainted boolean default false, " +
						"partial boolean default false );");
			}

		} catch (SQLException e) {
			logger.critical("Database connection failed");
			logger.critical(e.getMessage());
		}
	}

	public static void addNonSSLRecord(String ip) {
		addRecord(ip, false, false, false, false);
	}

	public static void addRecord(String ip, boolean isSSL, boolean leaky, boolean tainted, boolean partial) {

		CommonIdentifier identifier = IdentifierHelper.identifyHardware(ip);

		if (connection == null) {
			logger.critical("No connection from database.");
			return;
		}

		try {
			PreparedStatement statement = connection.prepareStatement(
					"insert into " + table + "(ip, identifier, ssl_enabled, leaky, tainted, partial) " +
							"values (?, ?, ?, ?, ?, ?);");

			statement.setString(1, ip);
			statement.setString(2, identifier.tag());
			statement.setBoolean(3, isSSL);
			statement.setBoolean(4, leaky);
			statement.setBoolean(5, tainted);
			statement.setBoolean(6, partial);

			statement.executeUpdate();

			logger.ok(String.format("Record %s inserted", ip));

		} catch (SQLException e) {
			logger.critical("record insertion failed");
			logger.critical(e.getMessage());
		}
	}
}
