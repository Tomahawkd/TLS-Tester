package io.tomahawkd.tlstester.database;

/**
 * Constants for database table, column and type
 */
public class RecorderConstants {

	/**
	 * Table data for data recording
	 */
	public static final String TABLE_DATA = "data";
	/**
	 * View statistic for statistic referring table data
	 */
	public static final String TABLE_STATISTIC = "statistic";
	/**
	 * View device for statistic referring table data
	 */
	public static final String TABLE_DEVICE = "device";
	/**
	 * View detailed statistic for every bit result from table data
	 */
	public static final String TABLE_DETAIL = "detail";

	/**
	 * host string
	 */
	public static final String COLUMN_HOST = "host";
	/**
	 * country code
	 */
	public static final String COLUMN_COUNTRY = "country";
	/**
	 * identifier tag
	 */
	public static final String COLUMN_IDENTIFIER = "identifier";
	/**
	 * boolean if the host enabled ssl/tls
	 */
	public static final String COLUMN_SSL = "ssl_enabled";
	/**
	 * host cert hash
	 */
	public static final String COLUMN_HASH = "hash";

	/**
	 * total number in view statistic
	 */
	public static final String COLUMN_TOTAL = "total";

	/**
	 * type table
	 */
	public static final int TABLE = 0;
	/**
	 * type view
	 */
	public static final int VIEW = 1;
}
