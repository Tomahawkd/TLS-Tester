package io.tomahawkd.detect.database;

import io.tomahawkd.common.ThrowableBiConsumer;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.detect.TreeCode;

import java.sql.*;

public class NamedRecorder extends AbstractRecorder {

	protected final String table;

	private static final Logger logger = Logger.getLogger(NamedRecorder.class);

	NamedRecorder(Connection connection, String name) throws SQLException {

		super(connection);

		this.table = name;

		synchronized (this.connection) {
			String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name = '" + table + "';";
			Statement statement = this.connection.createStatement();
			ResultSet set = statement.executeQuery(sql);

			if (!set.next()) {
				this.connection.createStatement()
						.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + " (" +
								" ip text PRIMARY KEY," +
								" ssl_enabled boolean default false," +
								" leaky integer default 0," +
								" tainted integer default 0," +
								" partial integer default 0," +
								" hash text default '');");
			}
		}
	}

	@Override
	public void addRecord(String ip, boolean isSSL, TreeCode leaky, TreeCode tainted, TreeCode partial, String hash) {

		// this include port which we need to delete
		if (ip.contains(":")) ip = ip.substring(0, ip.indexOf(":"));

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
									"(ip, ssl_enabled, leaky, tainted, partial, hash) " +
									"values (?, ?, ?, ?, ?, ?);");

					ptmt.setString(1, ip);
					ptmt.setBoolean(2, isSSL);
					ptmt.setLong(3, leaky.getRaw());
					ptmt.setLong(4, tainted.getRaw());
					ptmt.setLong(5, partial.getRaw());
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
										"hash = ? " +
										"where ip = '" + ip + "';");

						ptmt.setBoolean(1, isSSL);
						ptmt.setLong(2, leaky.getRaw());
						ptmt.setLong(3, tainted.getRaw());
						ptmt.setLong(4, partial.getRaw());
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

	@Override
	public void postUpdate(ThrowableBiConsumer<Connection, String> function) throws Exception {
		function.accept(connection, table);
	}
}