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

			if (!set.next()) {
				connection.createStatement()
						.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + " ("
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

		// this include port which we need to delete
		try {
			ip = ip.substring(0, ip.indexOf(":"));
		} catch (IndexOutOfBoundsException e) {
			logger.critical("Wrap ip " + ip + " failed, skipping");
			return;
		}

		CommonIdentifier identifier = IdentifierHelper.identifyHardware(ip);
		if (identifier == null) {
			logger.critical("Skip recording ip " + ip);
			return;
		}

		if (connection == null) {
			logger.critical("No connection from database.");
			return;
		}

		try {

			String sql = "SELECT * FROM " + table + " WHERE ip='" + ip + "';";
			ResultSet resultSet = connection.createStatement().executeQuery(sql);

			if (!resultSet.next()) {

				PreparedStatement ptmt = connection.prepareStatement(
						"insert into " + table + "(ip, identifier, ssl_enabled, leaky, tainted, partial) " +
								"values (?, ?, ?, ?, ?, ?);");

				ptmt.setString(1, ip);
				ptmt.setString(2, identifier.tag());
				ptmt.setBoolean(3, isSSL);
				ptmt.setBoolean(4, leaky);
				ptmt.setBoolean(5, tainted);
				ptmt.setBoolean(6, partial);

				ptmt.executeUpdate();
				logger.ok(String.format("Record %s inserted", ip));
			} else {

				PreparedStatement ptmt = connection.prepareStatement(
						"update " + table +
								" set identifier = ?, ssl_enabled = ?, leaky = ?, tainted = ?, partial = ?" +
								" where ip = '" + ip + "';");

				ptmt.setString(1, identifier.tag());
				ptmt.setBoolean(2, isSSL);
				ptmt.setBoolean(3, leaky);
				ptmt.setBoolean(4, tainted);
				ptmt.setBoolean(5, partial);

				ptmt.executeUpdate();
				logger.ok(String.format("Record %s updated", ip));
			}


		} catch (SQLException e) {
			logger.critical("record insertion failed");
			logger.critical(e.getMessage());
		}
	}
}
