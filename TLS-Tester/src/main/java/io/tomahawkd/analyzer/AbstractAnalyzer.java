package io.tomahawkd.analyzer;

import io.tomahawkd.data.TargetInfo;

import java.util.Map;

public abstract class AbstractAnalyzer implements Analyzer {

	@Override
	public abstract void analyze(TargetInfo info, TreeCode code);

	@Override
	public void preAnalyze(TargetInfo info,
	                       Map<Class<? extends Analyzer>, TreeCode>
			                       dependencyResults, TreeCode code) {}

	@Override
	public void postAnalyze(TargetInfo info, TreeCode code) {}

	public abstract String getResultDescription(TreeCode code);

	public abstract boolean getResult(TreeCode code);
}
