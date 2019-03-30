package org.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

/**
 * A JSONTokener takes a source string and extracts characters and tokens from
 * it. It is used by the JSONObject and JSONArray constructors to parse
 * JSON source strings.
 *
 * @author JSON.org
 * @version 2014-05-03
 */
public class JSONTokener {
	/**
	 * current read character position on the current line.
	 */
	private long character;
	/**
	 * flag to indicate if the end of the input has been found.
	 */
	private boolean eof;
	/**
	 * current read index of the input.
	 */
	private long index;
	/**
	 * current line of the input.
	 */
	private long line;
	/**
	 * previous character read from the input.
	 */
	private char previous;
	/**
	 * Reader for the input.
	 */
	private final Reader reader;
	/**
	 * flag to indicate that a previous character was requested.
	 */
	private boolean usePrevious;
	/**
	 * the number of characters read in the previous line.
	 */
	private long characterPreviousLine;


	/**
	 * Construct a JSONTokener from a Reader. The caller must close the Reader.
	 *
	 * @param reader A reader.
	 */
	private JSONTokener(Reader reader) {
		this.reader = reader.markSupported()
				? reader
				: new BufferedReader(reader);
		this.eof = false;
		this.usePrevious = false;
		this.previous = 0;
		this.index = 0;
		this.character = 1;
		this.characterPreviousLine = 0;
		this.line = 1;
	}


	/**
	 * Construct a JSONTokener from a string.
	 *
	 * @param s A source string.
	 */
	JSONTokener(String s) {
		this(new StringReader(s));
	}


	/**
	 * Back up one character. This provides a sort of lookahead capability,
	 * so that you can test for a digit or letter before attempting to parse
	 * the next number or identifier.
	 *
	 * @throws JSONException Thrown if trying to step back more than 1 step
	 *                       or if already at the start of the string
	 */
	void back() throws JSONException {
		if (this.usePrevious || this.index <= 0) {
			throw new JSONException("Stepping back two steps is not supported");
		}
		this.decrementIndexes();
		this.usePrevious = true;
		this.eof = false;
	}

	/**
	 * Decrements the indexes for the {@link #back()} method based on the previous character read.
	 */
	private void decrementIndexes() {
		this.index--;
		if (this.previous == '\r' || this.previous == '\n') {
			this.line--;
			this.character = this.characterPreviousLine;
		} else if (this.character > 0) {
			this.character--;
		}
	}

	/**
	 * Checks if the end of the input has been reached.
	 *
	 * @return true if at the end of the file and we didn't step back
	 */
	private boolean end() {
		return this.eof && !this.usePrevious;
	}


	/**
	 * Determine if the source string still contains characters that next()
	 * can consume.
	 *
	 * @return true if not yet at the end of the source.
	 * @throws JSONException thrown if there is an error stepping forward
	 *                       or backward while checking for more data.
	 */
	boolean more() throws JSONException {
		if (this.usePrevious) {
			return true;
		}
		try {
			this.reader.mark(1);
		} catch (IOException e) {
			throw new JSONException("Unable to preserve stream position", e);
		}
		try {
			// -1 is EOF, but next() can not consume the null character '\0'
			if (this.reader.read() <= 0) {
				this.eof = true;
				return false;
			}
			this.reader.reset();
		} catch (IOException e) {
			throw new JSONException("Unable to read the next character from the stream", e);
		}
		return true;
	}


	/**
	 * Get the next character in the source string.
	 *
	 * @return The next character, or 0 if past the end of the source string.
	 * @throws JSONException Thrown if there is an error reading the source string.
	 */
	char next() throws JSONException {
		int c;
		if (this.usePrevious) {
			this.usePrevious = false;
			c = this.previous;
		} else {
			try {
				c = this.reader.read();
			} catch (IOException exception) {
				throw new JSONException(exception);
			}
		}
		if (c <= 0) { // End of stream
			this.eof = true;
			return 0;
		}
		this.incrementIndexes(c);
		this.previous = (char) c;
		return this.previous;
	}

	/**
	 * Increments the internal indexes according to the previous character
	 * read and the character passed as the current character.
	 *
	 * @param c the current character read.
	 */
	private void incrementIndexes(int c) {
		if (c > 0) {
			this.index++;
			if (c == '\r') {
				this.line++;
				this.characterPreviousLine = this.character;
				this.character = 0;
			} else if (c == '\n') {
				if (this.previous != '\r') {
					this.line++;
					this.characterPreviousLine = this.character;
				}
				this.character = 0;
			} else {
				this.character++;
			}
		}
	}


	/**
	 * Get the next n characters.
	 *
	 * @param n The number of characters to take.
	 * @return A string of n characters.
	 * @throws JSONException Substring bounds error if there are not
	 *                       n characters remaining in the source string.
	 */

	@SuppressWarnings("all")
	private String next(int n) throws JSONException {
		if (n == 0) {
			return "";
		}

		char[] chars = new char[n];
		int pos = 0;

		while (pos < n) {
			chars[pos] = this.next();
			if (this.end()) {
				throw this.syntaxError("Substring bounds error");
			}
			pos += 1;
		}
		return new String(chars);
	}


	/**
	 * Get the next char in the string, skipping whitespace.
	 *
	 * @return A character, or 0 if there are no more characters.
	 * @throws JSONException Thrown if there is an error reading the source string.
	 */
	char nextClean() throws JSONException {
		for (; ; ) {
			char c = this.next();
			if (c == 0 || c > ' ') {
				return c;
			}
		}
	}


	/**
	 * Return the characters up to the next close quote character.
	 * Backslash processing is done. The formal JSON format does not
	 * allow strings in single quotes, but an implementation is allowed to
	 * accept them.
	 *
	 * @param quote The quoting character, either
	 *              <code>"</code>&nbsp;<small>(double quote)</small> or
	 *              <code>'</code>&nbsp;<small>(single quote)</small>.
	 * @return A String.
	 * @throws JSONException Unterminated string.
	 */
	private String nextString(char quote) throws JSONException {
		char c;
		StringBuilder sb = new StringBuilder();
		for (; ; ) {
			c = this.next();
			switch (c) {
				case 0:
				case '\n':
				case '\r':
					throw this.syntaxError("Unterminated string");
				case '\\':
					c = this.next();
					switch (c) {
						case 'b':
							sb.append('\b');
							break;
						case 't':
							sb.append('\t');
							break;
						case 'n':
							sb.append('\n');
							break;
						case 'f':
							sb.append('\f');
							break;
						case 'r':
							sb.append('\r');
							break;
						case 'u':
							try {
								sb.append((char) Integer.parseInt(this.next(4), 16));
							} catch (NumberFormatException e) {
								throw this.syntaxError("Illegal escape.", e);
							}
							break;
						case '"':
						case '\'':
						case '\\':
						case '/':
							sb.append(c);
							break;
						default:
							throw this.syntaxError("Illegal escape.");
					}
					break;
				default:
					if (c == quote) {
						return sb.toString();
					}
					sb.append(c);
			}
		}
	}


	/**
	 * Get the next value. The value can be a Boolean, Double, Integer,
	 * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
	 *
	 * @return An object.
	 * @throws JSONException If syntax error.
	 */
	Object nextValue() throws JSONException {
		char c = this.nextClean();
		String string;

		switch (c) {
			case '"':
			case '\'':
				return this.nextString(c);
			case '{':
				this.back();
				return new JSONObject(this);
			case '[':
				this.back();
				return new JSONArray(this);
		}

        /*
         * Handle unquoted text. This could be the values true, false, or
         * null, or it can be a number. An implementation (such as this one)
         * is allowed to also accept non-standard forms.
         *
         * Accumulate characters until we reach the end of the text or a
         * formatting character.
         */

		StringBuilder sb = new StringBuilder();
		while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
			sb.append(c);
			c = this.next();
		}
		this.back();

		string = sb.toString().trim();
		if ("".equals(string)) {
			throw this.syntaxError("Missing value");
		}
		return JSONObject.stringToValue(string);
	}


	/**
	 * Make a JSONException to signal a syntax error.
	 *
	 * @param message The error message.
	 * @return A JSONException object, suitable for throwing
	 */
	JSONException syntaxError(String message) {
		return new JSONException(message + this.toString());
	}

	/**
	 * Make a JSONException to signal a syntax error.
	 *
	 * @param message  The error message.
	 * @param causedBy The throwable that caused the error.
	 * @return A JSONException object, suitable for throwing
	 */

	@SuppressWarnings("all")
	private JSONException syntaxError(String message, Throwable causedBy) {
		return new JSONException(message + this.toString(), causedBy);
	}

	/**
	 * Make a printable string of this JSONTokener.
	 *
	 * @return " at {index} [character {character} line {line}]"
	 */
	@Override
	public String toString() {
		return " at " + this.index + " [character " + this.character + " line " +
				this.line + "]";
	}
}
