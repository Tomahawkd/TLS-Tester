package io.tomahawkd.tlstester.database;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class DriverDelegate implements Driver {

	private final Driver driver;

	public DriverDelegate(Driver driver) {
		this.driver = driver;
	}

	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		return driver.connect(url, info);
	}

	@Override
	public boolean acceptsURL(String url) throws SQLException {
		return driver.acceptsURL(url);
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		return driver.getPropertyInfo(url, info);
	}

	@Override
	public int getMajorVersion() {
		return driver.getMajorVersion();
	}

	@Override
	public int getMinorVersion() {
		return driver.getMinorVersion();
	}

	@Override
	public boolean jdbcCompliant() {
		return driver.jdbcCompliant();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return driver.getParentLogger();
	}
}
