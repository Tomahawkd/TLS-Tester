package io.tomahawkd.tlstester.provider;

import java.net.InetSocketAddress;
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
	InetSocketAddress getNextTarget();

	/**
	 * Add a target to storage
	 * @param data target to be tested
	 */
	void add(InetSocketAddress data);

	/**
	 * Add list of targets to storage
	 * @param data target list
	 */
	void addAll(Collection<InetSocketAddress> data);
}
