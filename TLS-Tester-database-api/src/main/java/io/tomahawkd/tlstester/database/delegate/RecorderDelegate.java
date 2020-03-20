package io.tomahawkd.tlstester.database.delegate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Constructor should either be Delegate(void) or Delegate(String user, String pass)
 */
public interface RecorderDelegate {

	String getUrl(String dbname);

	boolean checkTableExistence(String table, int type) throws SQLException;

	boolean checkMissingColumns(String table, List<String> list)
			throws SQLException;

	/**
	 * Execute before initializing database tables
	 * @param connection db connection instance
	 */
	void preInit(Connection connection) throws SQLException;

	void setDbName(String dbName);

	String getUsername();

	String getPassword();
}
