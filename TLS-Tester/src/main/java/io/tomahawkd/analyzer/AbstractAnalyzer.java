package io.tomahawkd.analyzer;

import io.tomahawkd.data.TargetInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractAnalyzer implements Analyzer {

	protected TreeCode code;
	protected List<Class<? extends Analyzer>> dependencies;

	AbstractAnalyzer(int length) {
		code = new TreeCode(length);
		dependencies = new ArrayList<>();
	}

	public abstract void analyze(TargetInfo info);

	@Override
	public void preAnalyze(TargetInfo info,
	                       Map<Class<? extends Analyzer>, ? extends Analyzer>
			                       dependencyResults) {}

	@Override
	public void postAnalyze(TargetInfo info) {}

	public abstract String getResultDescription();

	public abstract boolean getResult(TreeCode code);

	@Override
	public final boolean getResult() {
		return getResult(code);
	}

	@Override
	public final boolean hasDependencies() {
		return !dependencies.isEmpty();
	}

	@Override
	public final List<Class<? extends Analyzer>> getDependencies() {
		return dependencies;
	}

	public final TreeCode getCode() {
		return code;
	}
}
