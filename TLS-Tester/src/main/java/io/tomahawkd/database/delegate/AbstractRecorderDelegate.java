package io.tomahawkd.database.delegate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public abstract class AbstractRecorderDelegate implements RecorderDelegate {

	protected Connection connection;

	@Override
	public abstract String getUrl(String dbname);

	@Override
	public abstract boolean checkTableExistence(String table, int type) throws SQLException;

	@Override
	public abstract boolean checkMissingColumns(String table, List<String> list) throws SQLException;

	@Override
	public void preInit(Connection connection) throws SQLException {
		this.connection = connection;
	}

	@Override
	public String getUsername() {
		return null;
	}

	@Override
	public String getPassword() {
		return null;
	}
}
