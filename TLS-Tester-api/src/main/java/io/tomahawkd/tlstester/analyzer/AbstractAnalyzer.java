package io.tomahawkd.tlstester.analyzer;

import io.tomahawkd.tlstester.data.TargetInfo;
import io.tomahawkd.tlstester.data.TreeCode;

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

	@Override
	public abstract String getResultDescription(TreeCode code);

	@Override
	public abstract boolean getResult(TreeCode code);

	@Override
	public TreeCode updateResult(TreeCode code) {
		return code;
	};
}
