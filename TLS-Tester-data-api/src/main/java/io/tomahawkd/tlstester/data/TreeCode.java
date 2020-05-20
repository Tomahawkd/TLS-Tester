package io.tomahawkd.tlstester.data;

import java.util.Objects;

/**
 * io.tomahawkd.tlstester.analyzer.Analyzer result data structure model
 */
public class TreeCode {

	/**
	 * Bit Map length. Maximum is {@link Long#SIZE}
	 */
	private int length;

	/**
	 * Bit Map data
	 */
	private long code;

	/**
	 * Default tree code constructor
	 */
	public TreeCode() {
		this.length = Long.SIZE;
		code = 0L;
	}

	/**
	 * Recommend tree code constructor
	 *
	 * @param length bit map length
	 */
	public TreeCode(int length) {
		this.length = length;
		code = 0L;
	}

	/**
	 * Tree code constructor for dump usage
	 *
	 * @param code   bitmap
	 * @param length bitmap length
	 * @throws IllegalArgumentException if the bitmap exceeded the length
	 */
	public TreeCode(long code, int length) throws IllegalArgumentException {

		if ((code >> length) != 0)
			throw new IllegalArgumentException("Invalid code " + Long.toString(code, 2));

		this.code = code;
		this.length = length;
	}

	/**
	 * Deep copy this tree code
	 *
	 * @return copied tree code
	 */
	public TreeCode dump() {
		return new TreeCode(code, length);
	}

	/**
	 * Get value for specific position
	 *
	 * @param position value's position in the bitmap
	 * @return boolean value
	 * @throws IndexOutOfBoundsException if the position exceeded the length
	 */
	public boolean get(int position) throws IndexOutOfBoundsException {

		position = length - position - 1;

		if (position >= length || position < 0)
			throw new IndexOutOfBoundsException("The position " + position + " is invalid");

		return (code >> position & 1L) != 0;
	}

	/**
	 * Set value for specific position
	 *
	 * @param position value's position in the bitmap
	 * @param value    boolean value
	 * @throws IndexOutOfBoundsException if the position exceeded the length
	 */
	public void set(boolean value, int position) throws IndexOutOfBoundsException {

		if (position >= length || position < 0)
			throw new IndexOutOfBoundsException("The position " + position + " is invalid");

		code ^= toCode(get(position) ^ value) << (length - position - 1);

	}

	/**
	 * Bit Map length. Maximum is {@link Long#SIZE}
	 */
	public int length() {
		return length;
	}

	/**
	 * Bit Map data
	 */
	public long getRaw() {
		return code;
	}

	/**
	 * Reset tree code to 0
	 */
	public void clear() {
		this.code = 0L;
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			builder.append(toCode(get(i)));
		}
		return builder.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TreeCode &&
				this.code == ((TreeCode) obj).code &&
				this.length == ((TreeCode) obj).length;
	}

	@Override
	public int hashCode() {
		return Objects.hash(length, code);
	}

	private long toCode(boolean value) {
		return value ? 1L : 0;
	}
}
