package io.tomahawkd.tlstester.data.testssl.parser;

public class PercentageResult extends CountableResult {

	private int available;

	PercentageResult(int available, int count, String position) {
		this(available, count, position, null);
	}

	PercentageResult(int available, int count, String position, String info) {
		super(count, position, info);
		this.available = available;
	}

	public int getAvailable() {
		return available;
	}

	@Override
	public String toString() {
		return "PercentageResult{" +
				"available=" + available +
				", count=" + count +
				", position='" + position + '\'' +
				", info='" + info + '\'' +
				'}';
	}
}
