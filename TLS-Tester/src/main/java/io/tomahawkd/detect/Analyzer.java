package io.tomahawkd.detect;

import io.tomahawkd.data.TargetInfo;

public interface Analyzer {

	void analyze(TargetInfo info);

	void preAnalyze(TargetInfo info);

	void postAnalyze(TargetInfo info);

	String getResultDescription();

	boolean getResult();

	public TreeCode getCode();
}
