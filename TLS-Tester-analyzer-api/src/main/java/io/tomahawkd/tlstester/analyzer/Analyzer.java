package io.tomahawkd.tlstester.analyzer;

import io.tomahawkd.tlstester.data.TargetInfo;
import io.tomahawkd.tlstester.data.TreeCode;

import java.util.Map;

public interface Analyzer {

	void analyze(TargetInfo info, TreeCode code);

	default void preAnalyze(TargetInfo info,
	                        Map<Class<? extends Analyzer>, TreeCode> dependencyResults,
	                        TreeCode code) {
	}

	default void postAnalyze(TargetInfo info, TreeCode code) {
	}

	String getResultDescription(TreeCode code);

	boolean getResult(TreeCode code);

	default TreeCode updateResult(TreeCode code) {
		return code;
	}
}
