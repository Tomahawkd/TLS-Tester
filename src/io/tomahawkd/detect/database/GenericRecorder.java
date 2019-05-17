package io.tomahawkd.detect.database;

import io.tomahawkd.common.log.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GenericRecorder extends AbstractRecorder {

	private static final String table = "generirc";

	private static final Logger logger = Logger.getLogger(GenericRecorder.class);

	GenericRecorder() throws SQLException {

		super();

		String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name = '" + table + "';";
		Statement statement = connection.createStatement();
		ResultSet set = statement.executeQuery(sql);

		if (!set.next()) {
			connection.createStatement()
					.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + " (" +
							" ip text PRIMARY KEY," +
							" ssl_enabled boolean default false," +
							" leaky boolean default false," +
							" tainted boolean default false," +
							" partial boolean default false," +
							" hash text);");
		}

	}

	public synchronized void addRecord(String ip,
	                                   boolean isSSL,
	                                   boolean leaky, boolean tainted, boolean partial, String hash) {

		// this include port which we need to delete
		if (ip.contains(":")) ip = ip.substring(0, ip.indexOf(":"));

		if (connection == null) {
			logger.critical("No connection from database.");
			return;
		}

		try {

			String sql = "SELECT * FROM " + table + " WHERE ip='" + ip + "';";
			ResultSet resultSet = connection.createStatement().executeQuery(sql);

			if (!resultSet.next()) {

				PreparedStatement ptmt = connection.prepareStatement(
						"insert into " + table +
								"(ip, ssl_enabled, leaky, tainted, partial, hash) " +
								"values (?, ?, ?, ?, ?, ?);");

				ptmt.setString(1, ip);
				ptmt.setBoolean(2, isSSL);
				ptmt.setBoolean(3, leaky);
				ptmt.setBoolean(4, tainted);
				ptmt.setBoolean(5, partial);
				ptmt.setString(6, hash);

				ptmt.executeUpdate();
				logger.ok(String.format("Record %s inserted", ip));
			} else {

				if (isSSL || !resultSet.getBoolean("ssl_enabled")) {

					PreparedStatement ptmt = connection.prepareStatement(
							"update " + table +
									" set ssl_enabled = ?, " +
									"leaky = ?, " +
									"tainted = ?, " +
									"partial = ?, " +
									" where ip = '" + ip + "';");

					ptmt.setBoolean(1, isSSL);
					ptmt.setBoolean(2, leaky);
					ptmt.setBoolean(3, tainted);
					ptmt.setBoolean(4, partial);
					ptmt.setString(5, hash);

					ptmt.executeUpdate();
					logger.ok(String.format("Record %s updated", ip));
				}
			}


		} catch (SQLException e) {
			logger.critical("record insertion failed");
			logger.critical(e.getMessage());
		}
	}
}
