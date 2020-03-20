package io.tomahawkd.tlstester.data.testssl.parser;

public class CountableResult {

	protected int count;
	protected String position;
	protected String info;

	CountableResult(int count, String position) {
		this(count, position, null);
	}

	CountableResult(int count, String position, String info) {
		this.count = count;
		this.position = position;
		this.info = info;
	}

	public int getCount() {
		return count;
	}

	public String getPosition() {
		return position;
	}

	public String getInfo() {
		return info;
	}

	@Override
	public String toString() {
		return "CountableResult{" +
				"count=" + count +
				", position='" + position + '\'' +
				", info='" + info + '\'' +
				'}';
	}
}
