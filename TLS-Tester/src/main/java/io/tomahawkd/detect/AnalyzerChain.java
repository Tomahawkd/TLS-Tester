package io.tomahawkd.detect;

import io.tomahawkd.common.log.Logger;
import io.tomahawkd.data.TargetInfo;

import java.util.List;

public class AnalyzerChain extends AbstractAnalyzer {

	private static final Logger logger = Logger.getLogger(MainAnalyzer.class);


	private List<Analyzer> analyzers;
	private StringBuilder result;

	public AnalyzerChain(int length) {
		super(length);
	}

	@Override
	public void analyze(TargetInfo info) {

	}

	@Override
	public void preAnalyze(TargetInfo info) {

	}

	@Override
	public void postAnalyze(TargetInfo info) {

	}

	@Override
	public TreeCode getCode() {
		return super.getCode();
	}

	@Override
	public String getResultDescription() {
		return null;
	}

	@Override
	public boolean getResult() {
		return false;
	}
}
