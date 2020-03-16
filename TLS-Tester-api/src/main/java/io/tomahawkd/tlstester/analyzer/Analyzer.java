package io.tomahawkd.tlstester.analyzer;

import io.tomahawkd.tlstester.data.TargetInfo;
import io.tomahawkd.tlstester.data.TreeCode;

import java.util.Map;

public interface Analyzer {

	void analyze(TargetInfo info, TreeCode code);

	void preAnalyze(TargetInfo info,
	                Map<Class<? extends Analyzer>, TreeCode> dependencyResults,
	                TreeCode code);

	void postAnalyze(TargetInfo info, TreeCode code);

	String getResultDescription(TreeCode code);

	boolean getResult(TreeCode code);

	TreeCode updateResult(TreeCode code);
}
