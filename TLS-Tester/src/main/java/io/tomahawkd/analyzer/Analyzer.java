package io.tomahawkd.analyzer;

import io.tomahawkd.data.TargetInfo;

import java.util.List;
import java.util.Map;

public interface Analyzer {

	void analyze(TargetInfo info);

	void preAnalyze(TargetInfo info,
	                Map<Class<? extends Analyzer>, ? extends Analyzer> dependencyResults);

	void postAnalyze(TargetInfo info);

	boolean hasDependencies();

	List<Class<? extends Analyzer>> getDependencies();

	String getResultDescription();

	boolean getResult();

	TreeCode getCode();
}
