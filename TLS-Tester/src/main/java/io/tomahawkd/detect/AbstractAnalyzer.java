package io.tomahawkd.detect;

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

	public void preAnalyze(TargetInfo info,
	                       Map<Class<? extends Analyzer>, ? extends Analyzer>
			                       dependencyResults) {}

	public void postAnalyze(TargetInfo info) {}

	public abstract String getResultDescription();

	public abstract boolean getResult();

	public TreeCode getCode() {
		return code;
	}
}
