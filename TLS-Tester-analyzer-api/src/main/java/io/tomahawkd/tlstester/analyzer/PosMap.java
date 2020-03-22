package io.tomahawkd.tlstester.analyzer;

/**
 * Update src result to dst if they has same cert
 */
public @interface PosMap {

	/**
	 * Update boolean source position
	 */
	int src();

	/**
	 * Update boolean destination position
	 */
	int dst();
}
