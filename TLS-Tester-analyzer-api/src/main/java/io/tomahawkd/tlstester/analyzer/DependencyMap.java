package io.tomahawkd.tlstester.analyzer;

/**
 * Dependency map for analyzer
 */
public @interface DependencyMap {

	/**
	 * @return dependency type
	 */
	Class<? extends Analyzer> dep();

	/**
	 * @return tree code position for the dependency's conclusion
	 */
	int pos();
}
