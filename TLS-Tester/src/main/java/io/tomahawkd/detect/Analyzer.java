package io.tomahawkd.detect;

import io.tomahawkd.data.TargetInfo;

import java.util.Map;

public interface Analyzer {

	void analyze(TargetInfo info);

	void preAnalyze(TargetInfo info,
	                Map<Class<? extends Analyzer>, ? extends Analyzer> dependencyResults);

	void postAnalyze(TargetInfo info);

	String getResultDescription();

	boolean getResult();

	public TreeCode getCode();
}
