package io.tomahawkd.tlstester.database.delegate;

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
	private String dbName;
	private String username;
	private String password;

	public BaseRecorderDelegate() {
		this(null, null);
	}

	public BaseRecorderDelegate(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public abstract String getUrl();

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
	public final String getUsername() {
		return username;
	}

	@Override
	public final String getPassword() {
		return password;
	}

	@Override
	public final void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public final String getDbName() {
		return dbName;
	}

	public final ResultSet executeQuery(String q) throws SQLException {
		return this.connection.createStatement().executeQuery(q);
	}

	public final void executeUpdate(String q) throws SQLException {
		this.connection.createStatement().executeUpdate(q);
	}
}
