package io.tomahawkd.database.delegate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * You could use this class to prevent illegal connection usage like close
 * For more advanced usage please implement the interface directly
 */
public abstract class BaseRecorderDelegate implements RecorderDelegate {

	private Connection connection;

	@Override
	public abstract String getUrl(String dbname);

	@Override
	public abstract boolean checkTableExistence(String table, int type) throws SQLException;

	@Override
	public abstract boolean checkMissingColumns(String table, List<String> list) throws SQLException;

	protected void preInit() throws SQLException {

	}

	@Override
	public final void preInit(Connection connection) throws SQLException {
		this.connection = connection;
		preInit();
	}

	@Override
	public String getUsername() {
		return null;
	}

	@Override
	public String getPassword() {
		return null;
	}

	public ResultSet executeQuery(String q) throws SQLException {
		return this.connection.createStatement().executeQuery(q);
	}

	public void executeUpdate(String q) throws SQLException {
		this.connection.createStatement().executeUpdate(q);
	}
}
