package io.tomahawkd.detect;

public class TreeCode {

	private int length;
	private long code;

	public TreeCode(int length) {
		this.length = length;
		code = 0L;
	}

	public TreeCode(long code, int length) throws IllegalArgumentException {

		if ((code >> length) != 0)
			throw new IllegalArgumentException("Invalid code " + Long.toString(code, 2));

		this.code = code;
		this.length = length;
	}

	public boolean get(int position) throws IndexOutOfBoundsException {

		position = length - position - 1;

		if (position >= length || position < 0)
			throw new IndexOutOfBoundsException("The position " + position + " is invalid");

		return (code >> position & 1L) != 0;
	}

	public void set(boolean value, int position) throws IndexOutOfBoundsException {

		if (position >= length || position < 0)
			throw new IndexOutOfBoundsException("The position " + position + " is invalid");

		code ^= toCode(get(position) ^ value) << (length - position - 1);

	}

	public int length() {
		return length;
	}

	@Override
	public String toString() {
		return Long.toString(code, 2);
	}

	private long toCode(boolean value) {
		return value ? 1L : 0;
	}
}
