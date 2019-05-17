package io.tomahawkd.detect.database;

import com.fooock.shodan.model.host.Host;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.identifier.CommonIdentifier;
import io.tomahawkd.identifier.IdentifierHelper;

import java.sql.*;

public class StatisticRecoder extends AbstractRecorder {

	private static final String table = "statistic";

	private static final Logger logger = Logger.getLogger(StatisticRecoder.class);

	StatisticRecoder() throws SQLException {

		super();

		String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name = '" + table + "';";
		Statement statement = connection.createStatement();
		ResultSet set = statement.executeQuery(sql);

		if (!set.next()) {
			connection.createStatement()
					.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + " (" +
							" ip text PRIMARY KEY," +
							" identifier text not null, " +
							" ssl_enabled boolean default false," +
							" leaky boolean default false," +
							" tainted boolean default false," +
							" partial boolean default false," +
							" country text," +
							" hash text default '');");
		}
	}

	public synchronized void addRecord(String ip,
	                                          boolean isSSL,
	                                          boolean leaky, boolean tainted, boolean partial, String hash) {

		// this include port which we need to delete
		if (ip.contains(":")) ip = ip.substring(0, ip.indexOf(":"));

		Host host = IdentifierHelper.getInfoFromIp(ip);
		CommonIdentifier identifier = IdentifierHelper.identifyHardware(host);
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
						"insert into " + table +
								"(ip, identifier, ssl_enabled, leaky, tainted, partial, country, hash) " +
								"values (?, ?, ?, ?, ?, ?, ?, ?);");

				ptmt.setString(1, ip);
				ptmt.setString(2, identifier.tag());
				ptmt.setBoolean(3, isSSL);
				ptmt.setBoolean(4, leaky);
				ptmt.setBoolean(5, tainted);
				ptmt.setBoolean(6, partial);
				ptmt.setString(7, host.getCountryCode());
				ptmt.setString(8, hash);

				ptmt.executeUpdate();
				logger.ok(String.format("Record %s inserted", ip));
			} else {

				if (isSSL || !resultSet.getBoolean("ssl_enabled")) {

					PreparedStatement ptmt = connection.prepareStatement(
							"update " + table +
									" set identifier = ?, " +
									"ssl_enabled = ?, " +
									"leaky = ?, " +
									"tainted = ?, " +
									"partial = ?, " +
									"country = ?," +
									"hash = ?" +
									" where ip = '" + ip + "';");

					ptmt.setString(1, identifier.tag());
					ptmt.setBoolean(2, isSSL);
					ptmt.setBoolean(3, leaky);
					ptmt.setBoolean(4, tainted);
					ptmt.setBoolean(5, partial);
					ptmt.setString(6, host.getCountryCode());
					ptmt.setString(7, hash);

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