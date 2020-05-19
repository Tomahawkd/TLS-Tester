package io.tomahawkd.tlstester.provider.sources;

import io.tomahawkd.tlstester.extensions.ParameterizedExtensionPoint;
import io.tomahawkd.tlstester.provider.TargetStorage;

/**
 * Target Acquire source
 */
public interface TargetSource extends ParameterizedExtensionPoint {

	/**
	 * data acquire procedure, use {@link TargetStorage#add(java.net.InetSocketAddress)}
	 * or {@link TargetStorage#addAll(java.util.Collection)} to add target
	 *
	 * @param storage to store data
	 */
	void acquire(TargetStorage storage);
}
