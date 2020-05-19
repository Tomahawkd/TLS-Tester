package io.tomahawkd.tlstester.data;

import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.extensions.ExtensionPoint;

/**
 * <p>Data Collector interface.</p>
 *
 * <p>
 * The data collecting procedure happens before analyze procedure, data acquired from
 * this interface is later acquired by analyzer for analyzing usage.
 * </p>
 *
 * <p>
 * Since the acquired data structure could be various, the interface has to return
 * {@link Object} for more generic usage. In this case, I defined an annotation
 * {@link DataCollectTag} for a hint of the data's real type. And you may use this
 * annotation for declaring data tag for your collected data.
 *
 * Be advised that the data stores in a {@link java.util.Map} with a String key of tag
 * and your data as value. You must NOT use the tag in the {@link InternalNamespaces.Data}
 * to avoid overlap. The data with tags in the {@link InternalNamespaces.Data} contains
 * essential data needed by predefined Analyzers.
 * </p>
 *
 * <p>
 * For more information, see {@link TargetInfo}
 * </p>
 *
 * @see DataCollectTag
 * @see TargetInfo
 * @see InternalNamespaces.Data
 */
public interface DataCollector extends ExtensionPoint {

	/**
	 *
	 *
	 * @param host Host information storage class
	 * @return collected data
	 * @throws Exception potential exceptions need to handle by the program
	 * @see TargetInfo
	 */
	Object collect(TargetInfo host) throws Exception;
}
