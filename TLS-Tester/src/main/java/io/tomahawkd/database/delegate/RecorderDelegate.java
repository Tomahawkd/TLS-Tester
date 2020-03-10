package io.tomahawkd.database.delegate;

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

	void preInit(Connection connection) throws SQLException;

	String getUsername();

	String getPassword();
}
