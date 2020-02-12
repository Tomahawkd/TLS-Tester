package io.tomahawkd.testssl.data.parser;

public class OfferedResult {

	public static final String INVALID = "invalid";

	private boolean result;
	private String info;

	OfferedResult() {
		this.result = false;
		this.info = INVALID;
	}

	OfferedResult(boolean result, String info) {
		this.result = result;
		this.info = info;
	}

	public boolean isResult() {
		return result;
	}

	public String getInfo() {
		return info;
	}

	@Override
	public String toString() {
		return result + "[" + info + "]";
	}
}
