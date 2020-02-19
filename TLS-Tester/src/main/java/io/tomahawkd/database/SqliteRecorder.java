package io.tomahawkd.database;

import io.tomahawkd.analyzer.TreeCode;
import io.tomahawkd.data.TargetInfo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Database(name = "sqlite", type = DatabaseType.FILE, extension = ".sqlite.db")
@SuppressWarnings("unused")
public class SqliteRecorder extends AbstractRecorder {

	private boolean isTableMissing(String table) throws SQLException {
		String sql = "SELECT name FROM sqlite_master WHERE type='table' " +
				"AND name = '" + table + "';";
		Statement statement = this.connection.createStatement();
		ResultSet set = statement.executeQuery(sql);

		return !set.next();
	}

	private void checkColumns(String table, List<String> list) throws SQLException, RuntimeException {
		ResultSet s = this.connection.createStatement().executeQuery(
				"PRAGMA table_info(" + table + ");");
		while (s.next()) {
			if (!list.contains(s.getString("name"))) {
				throw new RuntimeException("Table " + table +
						" column mismatch, need to rebuild database.");
			}
		}
	}

	@Override
	protected void init() throws SQLException {

		if (isTableMissing(TABLE_DATA)) {
			// not exist
			StringBuilder sqlData = new StringBuilder();
			sqlData.append("CREATE TABLE IF NOT EXISTS ")
					.append("`").append(TABLE_DATA).append("`").append(" ( ")
					.append("`").append(COLUMN_HOST).append("`").append(" text PRIMARY KEY, ")
					.append("`").append(COLUMN_IDENTIFIER).append("`").append(" text not null, ")
					.append("`").append(COLUMN_COUNTRY).append("`").append(" text, ")
					.append("`").append(COLUMN_HASH).append("`")
					.append(" text default '").append(TargetInfo.CERT_HASH_NULL).append("', ")
					.append("`").append(COLUMN_SSL).append("`").append(" boolean default false, ");

			for (Record re : cachedList) {
				sqlData.append("`").append(re.column()).append("`").append(" integer default 0, ");
			}

			sqlData.delete(sqlData.length() - 2, sqlData.length()).append(" );");
			this.connection.createStatement().executeUpdate(sqlData.toString());
		} else {
			// existence check
			List<String> checkList = new ArrayList<>();
			checkList.add(COLUMN_HOST);
			checkList.add(COLUMN_IDENTIFIER);
			checkList.add(COLUMN_COUNTRY);
			checkList.add(COLUMN_HASH);
			checkList.add(COLUMN_SSL);
			for (Record re : cachedList) {
				checkList.add(re.column());
			}

			checkColumns(TABLE_DATA, checkList);
		}

		if (isTableMissing(TABLE_STATISTIC)) {
			// not exist
			StringBuilder sqlData = new StringBuilder();
			sqlData.append("CREATE TABLE IF NOT EXISTS ")
					.append("`").append(TABLE_STATISTIC).append("`").append(" ( ")
					.append("`").append(COLUMN_COUNTRY).append("`").append(" text PRIMARY KEY, ")
					.append("`").append(COLUMN_TOTAL).append("`").append(" integer default 0, ")
					.append("`").append(COLUMN_SSL).append("`").append(" integer default 0, ");

			for (Record c : cachedList) {
				if (c.map().length == 0)
					sqlData.append("`").append(c.column()).append("`").append(" integer default 0, ");
				else {
					for (StatisticMapping mapping : c.map()) {
						sqlData.append("`")
								.append(c.column()).append("_").append(mapping.column())
								.append("`").append(" integer default 0, ");
					}
				}
			}

			sqlData.delete(sqlData.length() - 2, sqlData.length()).append(" );");
			this.connection.createStatement().executeUpdate(sqlData.toString());
		} else {
			// existence check
			List<String> checkList = new ArrayList<>();
			checkList.add(COLUMN_HOST);
			checkList.add(COLUMN_COUNTRY);
			checkList.add(COLUMN_SSL);
			for (Record c : cachedList) {
				if (c.map().length == 0)
					checkList.add(c.column());
				else {
					for (StatisticMapping mapping : c.map()) {
						checkList.add(c.column() + "_" + mapping.column());
					}
				}
			}

			checkColumns(TABLE_STATISTIC, checkList);
		}
	}

	@Override
	public synchronized void record(TargetInfo info) {
		try {
			String sql =
					"SELECT * FROM " + TABLE_DATA + " WHERE " + COLUMN_HOST + "='" + info.getIp() + "';";
			ResultSet resultSet = connection.createStatement().executeQuery(sql);

			// not exist
			if (!resultSet.next()) {

				// insert
				PreparedStatement ptmt;
				if (info.isHasSSL()) {
					StringBuilder sqlData = new StringBuilder();
					sqlData.append("INSERT INTO ")
							.append("`").append(TABLE_DATA).append("`").append(" ( ")
							.append("`").append(COLUMN_HOST).append("`").append(", ")
							.append("`").append(COLUMN_IDENTIFIER).append("`").append(", ")
							.append("`").append(COLUMN_COUNTRY).append("`").append(", ")
							.append("`").append(COLUMN_HASH).append("`").append(", ")
							.append("`").append(COLUMN_SSL).append("`").append(", ");

					int questionMarkCounter = 0;
					for (Record re : cachedList) {
						sqlData.append("`").append(re.column()).append("`")
								.append(", ");
						questionMarkCounter++;
					}
					sqlData.delete(sqlData.length() - 2, sqlData.length()).append(" ) VALUES (");
					for (int i = 0; i < questionMarkCounter + 5; i++) {
						sqlData.append("?, ");
					}
					sqlData.delete(sqlData.length() - 2, sqlData.length()).append(" );");

					ptmt = connection.prepareStatement(sqlData.toString());

					ptmt.setString(1, info.getIp());
					ptmt.setString(2, info.getBrand());
					ptmt.setString(3, info.getHostInfo().getCountryCode());
					ptmt.setString(4, info.getCertHash());
					ptmt.setBoolean(5, info.isHasSSL());
					Map<String, TreeCode> result = info.getAnalysisResult();
					int index = 6;
					for (Record re : cachedList) {
						TreeCode code = result.get(re.column());
						if (code == null)
							throw new RuntimeException("Result of " + re.column() + " is missing");
						ptmt.setLong(index, code.getRaw());
						index++;
					}
				} else {
					// no ssl connection
					ptmt = connection.prepareStatement(
							"INSERT INTO " + "`" + TABLE_DATA + "`" + " ( "
									+ "`" + COLUMN_HOST + "`" + ", "
									+ "`" + COLUMN_IDENTIFIER + "`" + ", "
									+ "`" + COLUMN_COUNTRY + "`" + ") "
									+ "VALUES (?, ?, ?)");
					ptmt.setString(1, info.getIp());
					ptmt.setString(2, info.getBrand());
					ptmt.setString(3, info.getHostInfo().getCountryCode());
				}

				ptmt.executeUpdate();
			} else {

				// update
				if (info.isHasSSL() || !resultSet.getBoolean(COLUMN_SSL)) {

					StringBuilder sqlData = new StringBuilder();
					sqlData.append("UPDATE ").append("`").append(TABLE_DATA).append("`").append(" SET ")
							.append("`").append(COLUMN_IDENTIFIER).append("`").append(" = ?, ")
							.append("`").append(COLUMN_COUNTRY).append("`").append(" = ?, ")
							.append("`").append(COLUMN_HASH).append("`").append(" = ?, ")
							.append("`").append(COLUMN_SSL).append("`").append(" = ?, ");

					int questionMarkCounter = 0;
					for (Record re : cachedList) {
						sqlData.append("`").append(re.column()).append("`")
								.append(" = ?, ");
						questionMarkCounter++;
					}
					sqlData.delete(sqlData.length() - 2, sqlData.length())
							.append(" where ").append(COLUMN_HOST)
							.append(" = '").append(info.getIp()).append("';");

					PreparedStatement ptmt = connection.prepareStatement(sqlData.toString());

					ptmt.setString(1, info.getBrand());
					ptmt.setString(2, info.getHostInfo().getCountryCode());
					ptmt.setString(3, info.getCertHash());
					ptmt.setBoolean(4, info.isHasSSL());
					Map<String, TreeCode> result = info.getAnalysisResult();
					int index = 5;
					for (Record re : cachedList) {
						TreeCode code = result.get(re.column());
						if (code == null)
							throw new RuntimeException("Result of " + re.column() + " is missing");
						ptmt.setLong(index, code.getRaw());
						index++;
					}

					ptmt.executeUpdate();
				}
			}
		} catch (SQLException e) {

		}
	}
}
