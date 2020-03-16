package io.tomahawkd.tlstester.database;

import io.tomahawkd.tlstester.ArgParser;
import io.tomahawkd.tlstester.analyzer.Analyzer;
import io.tomahawkd.tlstester.analyzer.AnalyzerRunner;
import io.tomahawkd.tlstester.analyzer.TreeCode;
import io.tomahawkd.tlstester.annotations.PosMap;
import io.tomahawkd.tlstester.annotations.Record;
import io.tomahawkd.tlstester.annotations.StatisticMapping;
import io.tomahawkd.tlstester.common.log.Logger;
import io.tomahawkd.tlstester.data.DataHelper;
import io.tomahawkd.tlstester.data.TargetInfo;
import io.tomahawkd.tlstester.database.delegate.RecorderDelegate;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import java.sql.*;
import java.util.*;

public final class Recorder {

	private static final Logger logger = Logger.getLogger(Recorder.class);

	private Connection connection;
	private List<Record> cachedList;
	private RecorderDelegate delegate;

	public Recorder(@NotNull RecorderDelegate delegate) {

		logger.debug("Using delegate " + delegate.getClass().getName());
		this.delegate = delegate;
		logger.debug("Initializing database");

		if (!delegate.getClass().isAnnotationPresent(Database.class)) {
			logger.fatal("Database type is not declared in delegate: " + delegate.getClass().getName());
			throw new RuntimeException("No type declared in annotation");
		}

		if (!delegate.getClass().isAnnotationPresent(TypeMap.class)) {
			logger.fatal("Database type mapping is not declared in delegate: " +
					delegate.getClass().getName());
			throw new RuntimeException("No type mapping declared in annotation");
		}

		logger.debug("Caching recordable result metadata.");
		cachedList = new ArrayList<>();
		for (Class<?> clazz : new Reflections().getTypesAnnotatedWith(Record.class)) {
			if (Analyzer.class.isAssignableFrom(clazz)) {
				logger.debug("Caching class " + clazz.getName());
				cachedList.add(clazz.getAnnotation(Record.class));
			}
		}
		cachedList = Collections.unmodifiableList(cachedList);

		try {
			String url = delegate.getUrl(ArgParser.INSTANCE.get().getDbName());
			logger.debug("Database connection url constructed: " + url);
			connection = DriverManager.getConnection(url,
					delegate.getUsername(), delegate.getPassword());
			init();
		} catch (SQLException e) {
			logger.fatal("Database initialization failed.");
			logger.fatal(e.getMessage());
			throw new RuntimeException(e);
		}

		logger.debug("Database initialization complete.");
	}

	private void init() throws SQLException {

		logger.debug("Start init database");

		delegate.preInit(connection);

		TypeMap map = delegate.getClass().getAnnotation(TypeMap.class);
		if (delegate.checkTableExistence(RecorderConstants.TABLE_DATA, RecorderConstants.TABLE)) {
			// table column check

			logger.debug("Database exist, checking columns");
			List<String> checkList = new ArrayList<>();
			checkList.add(RecorderConstants.COLUMN_HOST);
			checkList.add(RecorderConstants.COLUMN_IDENTIFIER);
			checkList.add(RecorderConstants.COLUMN_COUNTRY);
			checkList.add(RecorderConstants.COLUMN_HASH);
			checkList.add(RecorderConstants.COLUMN_SSL);
			for (Record re : cachedList) {
				checkList.add(re.column());
			}
			if (delegate.checkMissingColumns(RecorderConstants.TABLE_DATA, checkList)) {
				logger.warn("Rebuild table " + RecorderConstants.TABLE_DATA);
				connection.createStatement().executeUpdate(
						"DROP TABLE " + RecorderConstants.TABLE_DATA + ";");
				createDataTable(map);
			}
		} else {
			createDataTable(map);
		}

		if (delegate.checkTableExistence(RecorderConstants.TABLE_STATISTIC, RecorderConstants.VIEW)) {
			List<String> checkList = new ArrayList<>();
			checkList.add(RecorderConstants.COLUMN_HOST);
			checkList.add(RecorderConstants.COLUMN_TOTAL);
			checkList.add(RecorderConstants.COLUMN_COUNTRY);
			checkList.add(RecorderConstants.COLUMN_SSL);
			for (Record re : cachedList) {
				if (re.map().length == 0)
					checkList.add(re.column());
				else {
					for (StatisticMapping mapping : re.map()) {
						checkList.add(re.column() + "_" + mapping.column());
					}
				}
			}
			if (delegate.checkMissingColumns(RecorderConstants.TABLE_STATISTIC, checkList)) {
				logger.warn("Rebuild table " + RecorderConstants.TABLE_STATISTIC);
				connection.createStatement().executeUpdate(
						"DROP VIEW " + RecorderConstants.TABLE_STATISTIC + ";");
				createStatisticView();
			}
		} else {
			createStatisticView();
		}

		logger.debug("Successfully initialize database");
	}

	private void createDataTable(TypeMap map) throws SQLException {
		StringBuilder sqlData = new StringBuilder();

		// data
		sqlData.append("CREATE TABLE ")
				.append("`").append(RecorderConstants.TABLE_DATA).append("`").append(" ( ")
				.append("`").append(RecorderConstants.COLUMN_HOST).append("` ")
				.append(map.string()).append(" PRIMARY KEY, ")
				.append("`").append(RecorderConstants.COLUMN_IDENTIFIER).append("` ")
				.append(map.string()).append(" not null, ")
				.append("`").append(RecorderConstants.COLUMN_COUNTRY).append("` ")
				.append(map.string()).append(", ")
				.append("`").append(RecorderConstants.COLUMN_HASH)
				.append("` ").append(map.string()).append(", ")
				.append("`").append(RecorderConstants.COLUMN_SSL).append("` ")
				.append(map.bool()).append(" default false, ");

		for (Record re : cachedList) {
			sqlData.append("`").append(re.column()).append("` ")
					.append(map.integer()).append(" default 0, ");
		}

		sqlData.delete(sqlData.length() - 2, sqlData.length()).append(" );");
		logger.debug("Creating data table with sql: " + sqlData.toString());
		this.connection.createStatement().executeUpdate(sqlData.toString());
	}

	private void createStatisticView() throws SQLException {
		StringBuilder sqlData = new StringBuilder();
		sqlData.append("CREATE VIEW ")
				.append("`").append(RecorderConstants.TABLE_STATISTIC).append("`").append(" AS ")
				.append("SELECT ")
				.append("`").append(RecorderConstants.COLUMN_COUNTRY).append("`, ")
				// main column for statistic

				.append("count(`").append(RecorderConstants.COLUMN_HOST).append("`) AS `")
				// count total host tested
				.append(RecorderConstants.COLUMN_TOTAL).append("`, ")

				.append("sum(`").append(RecorderConstants.COLUMN_SSL).append("`) AS `")
				// count those has ssl
				.append(RecorderConstants.COLUMN_SSL).append("`, ");

		for (Record re : cachedList) {

			// treecode extraction:
			//          (treecode >> (length - target_position - 1)) & 1
			if (re.map().length == 0)
				sqlData.append("sum(`")
						.append(re.column()).append("` >> ").append(re.resultLength() - 1)
						.append(" & 1")
						.append(") AS `").append(re.column()).append("`, ");
			else {
				for (StatisticMapping mapping : re.map()) {

					// name concatenation:
					//   <analyzer_name>_<record_map_name>
					sqlData.append("sum(");

					for (int pos : mapping.position()) {
						sqlData.append("(`").append(re.column()).append("` >> ")
								.append(re.resultLength() - pos - 1).append(" & 1) | ");
					}

					sqlData.delete(sqlData.length() - 3, sqlData.length());
					sqlData.append(") AS `")
							.append(re.column()).append("_").append(mapping.column())
							.append("`, ");
				}
			}
		}

		sqlData.delete(sqlData.length() - 2, sqlData.length())
				.append(" FROM `").append(RecorderConstants.TABLE_DATA).append("` ")
				.append("GROUP BY `").append(RecorderConstants.COLUMN_COUNTRY)
				.append("`;");

		logger.debug("Creating statistic view with sql: " + sqlData.toString());
		this.connection.createStatement().executeUpdate(sqlData.toString());
	}

	public void record(TargetInfo info) {
		// only add record, since statistic is a view
		try {
			String sql =
					"SELECT * FROM " + RecorderConstants.TABLE_DATA +
							" WHERE " + RecorderConstants.COLUMN_HOST +
							"='" + info.getHost() + "';";
			ResultSet resultSet = connection.createStatement().executeQuery(sql);

			// not exist
			if (!resultSet.next()) {

				// insert
				PreparedStatement ptmt;
				if (DataHelper.isHasSSL(info)) {
					StringBuilder sqlData = new StringBuilder();
					sqlData.append("INSERT INTO ")
							.append("`").append(RecorderConstants.TABLE_DATA).append("`")
							.append(" ( ")
							.append("`").append(RecorderConstants.COLUMN_HOST).append("`").append(", ")
							.append("`").append(RecorderConstants.COLUMN_IDENTIFIER).append("`")
							.append(", ")
							.append("`").append(RecorderConstants.COLUMN_COUNTRY).append("`")
							.append(", ")
							.append("`").append(RecorderConstants.COLUMN_HASH).append("`").append(", ")
							.append("`").append(RecorderConstants.COLUMN_SSL).append("`").append(", ");

					int questionMarkCounter = 0;
					for (Record re : cachedList) {
						sqlData.append("`").append(re.column()).append("`").append(", ");
						questionMarkCounter++;
					}

					sqlData.delete(sqlData.length() - 2, sqlData.length()).append(" ) VALUES (");
					for (int i = 0; i < questionMarkCounter + 5; i++) sqlData.append("?, ");
					sqlData.delete(sqlData.length() - 2, sqlData.length()).append(" );");

					logger.debug("Constructed sql: " + sqlData.toString());
					ptmt = connection.prepareStatement(sqlData.toString());

					ptmt.setString(1, info.getHost()); // host
					ptmt.setString(2, DataHelper.getBrand(info)); // identifier
					ptmt.setString(3, DataHelper.getCountryCode(info)); // country
					ptmt.setString(4, DataHelper.getCertHash(info)); // hash
					ptmt.setBoolean(5, DataHelper.isHasSSL(info)); // ssl
					Map<String, TreeCode> result = info.getAnalysisResult();
					int index = 6;
					for (Record re : cachedList) {
						TreeCode code = Objects.requireNonNull(result.get(re.column()),
								"Result of " + re.column() + " is missing");
						ptmt.setLong(index, code.getRaw());
						index++;
					}
				} else {
					// no ssl connection
					ptmt = connection.prepareStatement(
							"INSERT INTO `" + RecorderConstants.TABLE_DATA + "` ( "
									+ "`" + RecorderConstants.COLUMN_HOST + "`, "
									+ "`" + RecorderConstants.COLUMN_IDENTIFIER + "`, "
									+ "`" + RecorderConstants.COLUMN_COUNTRY + "`, "
									+ "`" + RecorderConstants.COLUMN_HASH +
									"`) VALUES (?, ?, ?, ?)");
					ptmt.setString(1, info.getHost());
					ptmt.setString(2, DataHelper.getBrand(info));
					ptmt.setString(3, DataHelper.getCountryCode(info));
					ptmt.setString(4, DataHelper.getCertHash(info));
				}

				ptmt.executeUpdate();
			} else {

				// update
				if (DataHelper.isHasSSL(info)) {

					StringBuilder sqlData = new StringBuilder();
					sqlData.append("UPDATE `").append(RecorderConstants.TABLE_DATA).append("`")
							.append(" SET ")
							.append("`").append(RecorderConstants.COLUMN_IDENTIFIER).append("`")
							.append(" = ?, ")
							.append("`").append(RecorderConstants.COLUMN_COUNTRY).append("`")
							.append(" = ?, ")
							.append("`").append(RecorderConstants.COLUMN_HASH).append("`")
							.append(" = ?, ")
							.append("`").append(RecorderConstants.COLUMN_SSL).append("`")
							.append(" = ?, ");

					for (Record re : cachedList) {
						sqlData.append("`").append(re.column()).append("`")
								.append(" = ?, ");
					}
					sqlData.delete(sqlData.length() - 2, sqlData.length())
							.append(" where ").append(RecorderConstants.COLUMN_HOST)
							.append(" = '").append(info.getHost()).append("';");

					logger.debug("Constructed sql: " + sqlData.toString());
					PreparedStatement ptmt = connection.prepareStatement(sqlData.toString());

					ptmt.setString(1, DataHelper.getBrand(info)); // identifier
					ptmt.setString(2, DataHelper.getCountryCode(info)); // country
					ptmt.setString(3, DataHelper.getCertHash(info)); // hash
					ptmt.setBoolean(4, DataHelper.isHasSSL(info)); // ssl
					Map<String, TreeCode> result = info.getAnalysisResult();
					int index = 5;
					for (Record re : cachedList) {
						TreeCode code = Objects.requireNonNull(result.get(re.column()),
								"Result of " + re.column() + " is missing");
						ptmt.setLong(index, code.getRaw());
						index++;
					}

					ptmt.executeUpdate();
				}
			}

			logger.debug("Recording complete");
		} catch (SQLException e) {
			logger.warn("Target " + info.getHost() + " update to database failed, abort update.");
			logger.warn(e.getMessage());
		}
	}

	/**
	 * In this place, we have 2 things to do:<br>
	 * 1. Update result from host which has same cert (horizontal)<br>
	 * 2. Update result from dependencies (vertical) by
	 * invoking {@link io.tomahawkd.tlstester.analyzer.AnalyzerRunner#updateResult}<br>
	 */
	public void postRecord() {
		try {

			// 1. Update result from host which has same cert (horizontal)
			for (Record r : cachedList) {
				for (PosMap posMap : r.posMap()) {
					String sql =
							generatePostUpdateSql(r.column(), r.resultLength(),
									posMap.src(), posMap.dst());
					logger.debug("Constructed sql: " + sql);
					connection.createStatement().executeUpdate(sql);
				}
			}

			// 2. Update result from dependencies (vertical)
			StringBuilder sqlData = new StringBuilder();
			sqlData.append("SELECT `").append(RecorderConstants.COLUMN_HOST).append("`, ");
			for (Record r : cachedList) {
				sqlData.append("`").append(r.column()).append("`, ");
			}
			sqlData.delete(sqlData.length() - 2, sqlData.length())
					.append(" FROM `").append(RecorderConstants.TABLE_DATA).append("` ")
					.append(" WHERE `").append(RecorderConstants.COLUMN_SSL).append("`;");

			ResultSet set = connection.createStatement().executeQuery(sqlData.toString());

			while (set.next()) {
				Map<String, TreeCode> m = new HashMap<>();
				for (Record r : cachedList) {
					m.put(r.column(), new TreeCode(set.getLong(r.column()), r.resultLength()));
				}
				AnalyzerRunner.INSTANCE.updateResult(m);

				StringBuilder sql = new StringBuilder();
				sql.append("UPDATE `").append(RecorderConstants.TABLE_DATA).append("`").append(" SET ");

				for (Record re : cachedList) {
					sql.append("`").append(re.column()).append("`")
							.append(" = ?, ");
				}
				sql.delete(sql.length() - 2, sql.length())
						.append(" where ").append(RecorderConstants.COLUMN_HOST)
						.append(" = '").append(set.getString(RecorderConstants.COLUMN_HOST))
						.append("';");

				logger.debug("Constructed sql: " + sql.toString());
				PreparedStatement ptmt = connection.prepareStatement(sql.toString());

				int index = 1;
				for (Record re : cachedList) {
					TreeCode code = Objects.requireNonNull(m.get(re.column()),
							"Result of " + re.column() + " is missing");
					ptmt.setLong(index, code.getRaw());
					index++;
				}

				ptmt.executeUpdate();
			}

		} catch (SQLException e) {
			logger.warn("Post update to database failed, abort update.");
			logger.warn(e.getMessage());
		}
	}

	private String generatePostUpdateSql(String column, int length, int src, int dst) {
		return "UPDATE `" + RecorderConstants.TABLE_DATA + "` " +
				"SET `" + column + "` = `" + column + "` + " + (1 << (length - dst - 1)) +
				" where " + RecorderConstants.COLUMN_HOST + " in (" +
				"SELECT `" + RecorderConstants.COLUMN_HOST + "` from (" +
				"SELECT `" + RecorderConstants.COLUMN_HOST + "` " +
				"from `" + RecorderConstants.TABLE_DATA + "` " +
				"natural join (SELECT `" + RecorderConstants.COLUMN_HASH + "` " +
				"from `" + RecorderConstants.TABLE_DATA + "` " +
				"where `" + RecorderConstants.COLUMN_SSL + "` " +
				"GROUP BY `" + RecorderConstants.COLUMN_HASH + "` " +
				"HAVING count(`" + RecorderConstants.COLUMN_HOST + "`) > 1 " +
				"and sum((`" + column + "` >> " + (length - src - 1) + ") & 1) > 0 " +
				") as `inner_temp` " +
				"where ((`" + column + "` >> " + (length - src - 1) + ") & 1) = 0" +
				") as `outer_temp`);";
	}

	void close() {
		try {
			logger.debug("Closing connection.");
			connection.close();
		} catch (SQLException e) {
			logger.warn("Unable to close connection.");
		}
	}
}
