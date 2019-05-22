package io.tomahawkd.detect.database;

import com.fooock.shodan.model.host.Host;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.detect.LeakyChannelAnalyzer;
import io.tomahawkd.detect.PartiallyLeakyChannelAnalyzer;
import io.tomahawkd.detect.TaintedChannelAnalyzer;
import io.tomahawkd.detect.TreeCode;
import io.tomahawkd.identifier.IdentifierHelper;
import org.jetbrains.annotations.Contract;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StatisticRecorder extends AbstractRecorder {

	private static final String table = "statistic";

	private static final Logger logger = Logger.getLogger(StatisticRecorder.class);

	private List<String> targetTables = new ArrayList<>();

	StatisticRecorder(Connection connection) throws SQLException {
		super(connection);

		synchronized (this.connection) {
			String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name = '" + table + "';";
			Statement statement = this.connection.createStatement();
			ResultSet set = statement.executeQuery(sql);

			if (!set.next()) {
				this.connection.createStatement()
						.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + " (" +
								" country text PRIMARY KEY," +
								" ssl_enabled integer default 0," +
								" leaky integer default 0," +
								" tainted integer default 0," +
								" tainted_force_rsa integer default 0," +
								" tainted_learn_session integer default 0," +
								" tainted_forge_sign integer default 0," +
								" tainted_heartbleed integer default 0," +
								" partial integer default 0);");
			} else {
				this.connection.createStatement()
						.executeUpdate("DELETE FROM " + table + " WHERE 1;");
			}
		}
	}

	@Override
	public void addRecord(String ip, boolean isSSL, TreeCode leaky, TreeCode tainted, TreeCode partial, String hash) {

	}

	public void addPostRecord(String ip, boolean isSSL, TreeCode leaky, TreeCode tainted, TreeCode partial,
	                          String hash) {

		// this include port which we need to delete
		if (ip.contains(":")) ip = ip.substring(0, ip.indexOf(":"));

		Host host = IdentifierHelper.getInfoFromIp(ip);
		if (host == null) {
			logger.critical("No host information received, skipping");
			return;
		}

		if (connection == null) {
			logger.critical("No connection from database.");
			return;
		}

		synchronized (connection) {
			try {

				String country = host.getCountryCode() == null ? "null" : host.getCountryCode();

				String sql = "SELECT * FROM " + table + " WHERE country='" + country + "';";
				ResultSet resultSet = connection.createStatement().executeQuery(sql);

				if (!resultSet.next()) {

					PreparedStatement ptmt = connection.prepareStatement(
							"insert into " + table +
									"(country, ssl_enabled, " +
									"leaky, " + "tainted, " +
									"tainted_force_rsa, tainted_learn_session, tainted_forge_sign, tainted_heartbleed, " +
									"partial) " +
									"values (?, ?, ?, ?, ?, ?, ?, ?, ?);");

					ptmt.setString(1, country);
					ptmt.setInt(2, booleanToInt(isSSL));
					ptmt.setInt(3, booleanToInt(leaky.get(LeakyChannelAnalyzer.RSA_KEY_EXCHANGE_OFFLINE)));
					ptmt.setInt(4, booleanToInt(
							tainted.get(TaintedChannelAnalyzer.FORCE_RSA_KEY_EXCHANGE) ||
									tainted.get(TaintedChannelAnalyzer.LEARN_LONG_LIVE_SESSION) ||
									tainted.get(TaintedChannelAnalyzer.FORGE_RSA_SIGN) ||
									tainted.get(TaintedChannelAnalyzer.HEARTBLEED)));
					ptmt.setInt(5, booleanToInt(tainted.get(TaintedChannelAnalyzer.FORCE_RSA_KEY_EXCHANGE)));
					ptmt.setInt(6, booleanToInt(tainted.get(TaintedChannelAnalyzer.LEARN_LONG_LIVE_SESSION)));
					ptmt.setInt(7, booleanToInt(tainted.get(TaintedChannelAnalyzer.FORGE_RSA_SIGN)));
					ptmt.setInt(8, booleanToInt(tainted.get(TaintedChannelAnalyzer.HEARTBLEED)));
					ptmt.setInt(9, booleanToInt(partial.get(PartiallyLeakyChannelAnalyzer.CBC_PADDING)));

					ptmt.executeUpdate();
				} else {

					if (isSSL || !resultSet.getBoolean("ssl_enabled")) {

						PreparedStatement ptmt = connection.prepareStatement(
								"update " + table +
										" set ssl_enabled = ssl_enabled + ?, " +
										"leaky = leaky + ?, " +
										"tainted = tainted + ?, " +
										"tainted_force_rsa = tainted_force_rsa + ?, " +
										"tainted_learn_session = tainted_learn_session + ?, " +
										"tainted_forge_sign = tainted_forge_sign + ?, " +
										"tainted_heartbleed = tainted_heartbleed + ?," +
										"partial = partial + ?" +
										" where country = '" + country + "';");

						ptmt.setInt(1, booleanToInt(isSSL));
						ptmt.setInt(2, booleanToInt(leaky.get(LeakyChannelAnalyzer.RSA_KEY_EXCHANGE_OFFLINE)));
						ptmt.setInt(3, booleanToInt(
								tainted.get(TaintedChannelAnalyzer.FORCE_RSA_KEY_EXCHANGE) ||
										tainted.get(TaintedChannelAnalyzer.LEARN_LONG_LIVE_SESSION) ||
										tainted.get(TaintedChannelAnalyzer.FORGE_RSA_SIGN) ||
										tainted.get(TaintedChannelAnalyzer.HEARTBLEED)));
						ptmt.setInt(4, booleanToInt(tainted.get(TaintedChannelAnalyzer.FORCE_RSA_KEY_EXCHANGE)));
						ptmt.setInt(5, booleanToInt(tainted.get(TaintedChannelAnalyzer.LEARN_LONG_LIVE_SESSION)));
						ptmt.setInt(6, booleanToInt(tainted.get(TaintedChannelAnalyzer.FORGE_RSA_SIGN)));
						ptmt.setInt(7, booleanToInt(tainted.get(TaintedChannelAnalyzer.HEARTBLEED)));
						ptmt.setInt(8, booleanToInt(partial.get(PartiallyLeakyChannelAnalyzer.CBC_PADDING)));

						ptmt.executeUpdate();
					}
				}

				logger.ok(String.format("Record %s updated", ip));

			} catch (SQLException e) {
				logger.critical("record insertion failed");
				logger.critical(e.getMessage());
			}
		}
	}

	@Contract("_ -> this")
	public StatisticRecorder addTargetTable(String table) {
		targetTables.add(table);
		return this;
	}

	@Override
	public void postUpdate() {

		for (String targetTable : targetTables) {

			String sql = "SELECT * FROM " + targetTable;
			try {
				Statement statement = connection.createStatement();
				ResultSet set = statement.executeQuery(sql);

				while (set.next()) {

					String ip = set.getString("ip");
					boolean isSSL = set.getBoolean("ssl_enabled");
					TreeCode leaky =
							new TreeCode(set.getLong("leaky"), LeakyChannelAnalyzer.TREE_LENGTH);
					TreeCode tainted =
							new TreeCode(set.getLong("tainted"), TaintedChannelAnalyzer.TREE_LENGTH);
					TreeCode partial =
							new TreeCode(set.getLong("partial"),
									PartiallyLeakyChannelAnalyzer.TREE_LENGTH);
					String hash = set.getString("hash");

					addPostRecord(ip, isSSL, leaky, tainted, partial, hash);
				}
			} catch (Exception e) {
				logger.critical("record insertion failed, skipping table " + targetTable);
				logger.critical(e.getMessage());
			}

			logger.ok(String.format("Data from table %s updated", targetTable));
		}
	}

	private int booleanToInt(boolean value) {
		return value ? 1 : 0;
	}
}
