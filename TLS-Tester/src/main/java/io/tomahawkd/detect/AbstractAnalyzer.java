package io.tomahawkd.detect;

import io.tomahawkd.data.TargetInfo;

public abstract class AbstractAnalyzer implements Analyzer {

	protected TreeCode code;

	AbstractAnalyzer(int length) {
		code = new TreeCode(length);
	}

	public abstract void analyze(TargetInfo info);

	public abstract void preAnalyze(TargetInfo info);

	public abstract void postAnalyze(TargetInfo info);

	public abstract String getResultDescription();

	public abstract boolean getResult();

	public TreeCode getCode() {
		return code;
	}
}
