package io.tomahawkd.tlstester.identifier;

import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.extensions.ExtensionPoint;

/**
 * Identifier for identifying service/device
 */
public interface Identifier extends ExtensionPoint {

	/**
	 * Tag for identified service/device name
	 *
	 * @return tag
	 */
	String tag();

	/**
	 * identify the host
	 *
	 * @param host host data
	 * @return if service/device is identified by current Identifier
	 * @see Host
	 */
	boolean identify(Host host);
}
