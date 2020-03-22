package io.tomahawkd.tlstester.analyzer;

/**
 * Statistic column mapping
 */
public @interface StatisticMapping {

	/**
	 * @return part of statistic column name
	 */
	String column();

	/**
	 * result of positions' result with 'and' operation
	 */
	int[] position();
}
