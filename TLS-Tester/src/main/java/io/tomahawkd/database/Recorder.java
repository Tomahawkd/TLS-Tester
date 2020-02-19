package io.tomahawkd.database;

import io.tomahawkd.data.TargetInfo;

public interface Recorder {

	void record(TargetInfo info);



	// Constant data
	String TABLE_DATA = "data";
	String TABLE_STATISTIC = "statistic";

	String COLUMN_HOST = "host";
	String COLUMN_COUNTRY = "country";
	String COLUMN_IDENTIFIER = "identifier";
	String COLUMN_SSL = "ssl_enabled";
	String COLUMN_HASH = "hash";

	String COLUMN_TOTAL = "total";
}
