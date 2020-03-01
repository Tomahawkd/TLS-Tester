package io.tomahawkd.database;

import io.tomahawkd.data.TargetInfo;

public interface Recorder {

	void record(TargetInfo info);

	/**
	 * In this place, we have 2 things to do:<br>
	 *     1. Update result from host which has same cert (horizontal)<br>
	 *     2. Update result from dependencies (vertical) by
	 *     invoking {@link io.tomahawkd.analyzer.AnalyzerRunner#updateResult}<br>
	 */
	void postRecord();

	// Constant data
	String TABLE_DATA = "data";
	String TABLE_STATISTIC = "statistic";

	String COLUMN_HOST = "host";
	String COLUMN_COUNTRY = "country";
	String COLUMN_IDENTIFIER = "identifier";
	String COLUMN_SSL = "ssl_enabled";
	String COLUMN_HASH = "hash";

	String COLUMN_TOTAL = "total";

	int TABLE = 0;
	int VIEW = 1;
}
