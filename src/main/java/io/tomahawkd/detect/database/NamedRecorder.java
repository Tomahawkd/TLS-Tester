package io.tomahawkd.detect.database;

import io.tomahawkd.common.log.Logger;
import io.tomahawkd.detect.LeakyChannelAnalyzer;
import io.tomahawkd.detect.TaintedChannelAnalyzer;
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
	public void postUpdate() throws SQLException {

		synchronized (this.connection) {
			String sql = "SELECT DISTINCT hash FROM " + table + ";";
			Statement searchStmt = this.connection.createStatement();
			ResultSet set = searchStmt.executeQuery(sql);
			while (set.next()) {
				String hash = set.getString("hash");
				if (hash.isEmpty()) continue;

				boolean isLeaky = false;
				boolean isTainted_Force = false;
				boolean isTainted_Forge = false;

				String query = "SELECT leaky, tainted FROM " + table + " WHERE hash = '" + hash + "';";
				Statement checkStmt = this.connection.createStatement();
				ResultSet list = checkStmt.executeQuery(query);
				while (list.next()) {
					TreeCode leaky = new TreeCode(list.getLong("leaky"), LeakyChannelAnalyzer.TREE_LENGTH);
					logger.debug("Leaky tree code " + leaky);
					isLeaky = leaky.get(LeakyChannelAnalyzer.RSA_DECRYPTION_HOST);

					TreeCode tainted = new TreeCode(list.getLong("tainted"), TaintedChannelAnalyzer.TREE_LENGTH);
					logger.debug("Tainted tree code " + tainted);
					isTainted_Force = tainted.get(TaintedChannelAnalyzer.RSA_DECRYPTION_HOST);
					isTainted_Forge = tainted.get(TaintedChannelAnalyzer.RSA_SIGN_HOST);
				}

				query = "SELECT * FROM " + table + " WHERE hash = '" + hash + "';";
				Statement updateStmt = this.connection.createStatement();
				list = updateStmt.executeQuery(query);
				while (list.next()) {

					TreeCode leaky = new TreeCode(list.getLong("leaky"), LeakyChannelAnalyzer.TREE_LENGTH);
					leaky.set(isLeaky, LeakyChannelAnalyzer.RSA_DECRYPTION_OTHER);
					logger.debug("Leaky tree code set to " + leaky);
					LeakyChannelAnalyzer.update(leaky);

					TreeCode tainted = new TreeCode(list.getLong("tainted"), TaintedChannelAnalyzer.TREE_LENGTH);
					tainted.set(isTainted_Force, TaintedChannelAnalyzer.RSA_DECRYPTION_OTHER);
					tainted.set(isTainted_Forge, TaintedChannelAnalyzer.RSA_SIGN_OTHER);
					logger.debug("Tainted tree code set to " + tainted);
					TaintedChannelAnalyzer.update(tainted);

					String ip = list.getString("ip");
					PreparedStatement ptmt = connection.prepareStatement(
							"update " + table +
									" set leaky = ?, " +
									"tainted = ? " +
									" where ip = '" + ip + "';");

					ptmt.setLong(1, leaky.getRaw());
					ptmt.setLong(2, tainted.getRaw());

					ptmt.executeUpdate();

					logger.info(String.format("IP %s updated", ip));
				}
			}
		}
	}
}
