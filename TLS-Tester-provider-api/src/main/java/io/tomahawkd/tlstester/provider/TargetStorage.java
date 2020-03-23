package io.tomahawkd.tlstester.provider;

import java.util.Collection;

/**
 * Target Storage class storing target
 */
public interface TargetStorage {

	/**
	 * Get if the storage has more target
	 *
	 * @return true if has
	 */
	boolean hasMoreData();

	/**
	 * Get data
	 * @return target string
	 */
	String getNextTarget();

	/**
	 * Add a target to storage
	 * @param data target to be tested
	 */
	void add(String data);

	/**
	 * Add list of targets to storage
	 * @param data target list
	 */
	void addAll(Collection<String> data);
}
