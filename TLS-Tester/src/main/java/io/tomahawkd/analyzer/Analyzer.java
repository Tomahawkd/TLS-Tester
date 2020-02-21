package io.tomahawkd.analyzer;

import io.tomahawkd.data.TargetInfo;

import java.util.List;
import java.util.Map;

public interface Analyzer {

	void analyze(TargetInfo info, TreeCode code);

	void preAnalyze(TargetInfo info,
	                Map<Class<? extends Analyzer>, TreeCode> dependencyResults,
	                TreeCode code);

	void postAnalyze(TargetInfo info, TreeCode code);

	boolean hasDependencies();

	List<Class<? extends Analyzer>> getDependencies();

	String getResultDescription(TreeCode code);

	boolean getResult(TreeCode code);
}
