package io.tomahawkd.detect.database;

import com.fooock.shodan.model.host.Host;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.detect.TreeCode;
import io.tomahawkd.identifier.CommonIdentifier;
import io.tomahawkd.identifier.IdentifierHelper;

import java.sql.*;

public class HardwareRecoder extends AbstractRecorder {

	private static final String table = "statistic";

	private static final Logger logger = Logger.getLogger(HardwareRecoder.class);

	HardwareRecoder(Connection connection) throws SQLException {

		super(connection);

		synchronized (this.connection) {
			String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name = '" + table + "';";
			Statement statement = this.connection.createStatement();
			ResultSet set = statement.executeQuery(sql);

			if (!set.next()) {
				this.connection.createStatement()
						.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + " (" +
								" ip text PRIMARY KEY," +
								" identifier text not null, " +
								" ssl_enabled boolean default false," +
								" leaky integer default 0," +
								" tainted integer default 0," +
								" partial integer default 0," +
								" country text," +
								" hash text default '');");
			}
		}
	}

	@Override
	public void addRecord(String ip, boolean isSSL, TreeCode leaky, TreeCode tainted, TreeCode partial, String hash) {

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

		synchronized (connection) {
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
					ptmt.setLong(4, leaky.getRaw());
					ptmt.setLong(5, tainted.getRaw());
					ptmt.setLong(6, partial.getRaw());
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
						ptmt.setLong(3, leaky.getRaw());
						ptmt.setLong(4, tainted.getRaw());
						ptmt.setLong(5, partial.getRaw());
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
}
