package io.tomahawkd.analyzer;

import io.tomahawkd.data.TargetInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractAnalyzer implements Analyzer {

	protected List<Class<? extends Analyzer>> dependencies;

	AbstractAnalyzer() {
		dependencies = new ArrayList<>();
	}

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

	@Override
	public final boolean hasDependencies() {
		return !dependencies.isEmpty();
	}

	@Override
	public final List<Class<? extends Analyzer>> getDependencies() {
		return dependencies;
	}
}
