package io.tomahawkd.tlstester.database.delegate;

import io.tomahawkd.tlstester.extensions.ParameterizedExtensionPoint;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Constructor should either be Delegate(void) or Delegate(String user, String pass)
 */
public interface RecorderDelegate extends ParameterizedExtensionPoint {

	/**
	 * Get connection url
	 * @return url
	 */
	String getUrl();

	/**
	 * Check if the table exist
	 *
	 * @param table table name
	 * @param type table type, should be either
	 * {@link io.tomahawkd.tlstester.database.RecorderConstants#TABLE} or
	 *             {@link io.tomahawkd.tlstester.database.RecorderConstants#VIEW}
	 * @return true if existed
	 * @throws SQLException sql exceptions
	 */
	boolean checkTableExistence(String table, int type) throws SQLException;

	/**
	 * Check if the columns is missing
	 * @param table table name
	 * @param list column names
	 * @return true if column is missing
	 * @throws SQLException sql exceptions
	 */
	boolean checkMissingColumns(String table, List<String> list)
			throws SQLException;

	/**
	 * Execute before initializing database tables
	 * @param connection db connection instance
	 */
	void preInit(Connection connection) throws SQLException;

	/**
	 * Set the database name received from commandline
	 *
	 * @param dbName database/schema name
	 */
	void setDbName(String dbName);

	/**
	 * @return Database username (if any).
	 */
	String getUsername();

	/**
	 * @return Database password (if any).
	 */
	String getPassword();
}
