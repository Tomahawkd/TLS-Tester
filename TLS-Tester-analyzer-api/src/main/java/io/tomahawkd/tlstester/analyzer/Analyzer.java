package io.tomahawkd.tlstester.analyzer;

import io.tomahawkd.tlstester.data.TargetInfo;
import io.tomahawkd.tlstester.data.TreeCode;
import io.tomahawkd.tlstester.extensions.ExtensionPoint;

import java.util.Map;

/**
 * Analyzer interface
 */
public interface Analyzer extends ExtensionPoint {

	/**
	 * Target analyze
	 *
	 * @param info information collected in the former procedure
	 * @param code analysis result
	 */
	void analyze(TargetInfo info, TreeCode code);

	/**
	 * preparation before analysis
	 *
	 * @param info information collected in the former procedure
	 * @param dependencyResults dependent analyzer results
	 * @param code analysis result
	 */
	default void preAnalyze(TargetInfo info,
	                        Map<Class<? extends Analyzer>, TreeCode> dependencyResults,
	                        TreeCode code) {
	}

	/**
	 * procedure after analysis
	 *
	 * @param info information collected in the former procedure
	 * @param code analyze result
	 */
	default void postAnalyze(TargetInfo info, TreeCode code) {
	}

	/**
	 * Description of the analysis
	 *
	 * @param code analysis result
	 * @return description
	 */
	String getResultDescription(TreeCode code);

	/**
	 * The final conclusion whether the target is vulnerable
	 *
	 * @param code analysis result
	 * @return true if the target is vulnerable
	 */
	boolean getResult(TreeCode code);

	/**
	 * Update result if it has update
	 *
	 * @param code analysis result
	 * @return updated codes
	 */
	default TreeCode updateResult(TreeCode code) {
		return code;
	}
}
