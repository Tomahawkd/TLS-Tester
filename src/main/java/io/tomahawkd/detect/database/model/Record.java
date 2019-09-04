package io.tomahawkd.detect.database.model;

import io.tomahawkd.detect.TreeCode;

public interface Record {

	String getIp();

	int getPort();

	String getCountry();

	boolean isSslEnabled();

	TreeCode getLeaky();

	TreeCode getTainted();

	TreeCode getPartial();

	String getHash();
}
