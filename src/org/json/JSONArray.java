package org.json;

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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A JSONArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL object</code>.
 * <p>
 * The constructor can convert a JSON text into a Java object. The
 * <code>toString</code> method converts to JSON text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * JSON syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there is <code>,</code>
 * &nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 * or single quote, and if they do not contain leading or trailing spaces, and
 * if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , #</code> and if they do not look like numbers and
 * if they are not the reserved words <code>true</code>, <code>false</code>, or
 * <code>null</code>.</li>
 * </ul>
 *
 * @author JSON.org
 * @version 2016-08/15
 */
public class JSONArray implements Iterable<Object> {

	/**
	 * The arrayList where the JSONArray's properties are kept.
	 */
	private final ArrayList<Object> myArrayList;

	/**
	 * Construct an empty JSONArray.
	 */
	JSONArray() {
		this.myArrayList = new ArrayList<>();
	}

	/**
	 * Construct a JSONArray from a JSONTokener.
	 *
	 * @param x A JSONTokener
	 * @throws JSONException If there is a syntax error.
	 */
	JSONArray(JSONTokener x) throws JSONException {
		this();
		if (x.nextClean() != '[') {
			throw x.syntaxError("A JSONArray text must start with '['");
		}
		if (x.nextClean() != ']') {
			x.back();
			for (; ; ) {
				if (x.nextClean() == ',') {
					x.back();
					this.myArrayList.add(JSONObject.NULL);
				} else {
					x.back();
					this.myArrayList.add(x.nextValue());
				}
				switch (x.nextClean()) {
					case ',':
						if (x.nextClean() == ']') {
							return;
						}
						x.back();
						break;
					case ']':
						return;
					default:
						throw x.syntaxError("Expected a ',' or ']'");
				}
			}
		}
	}

	/**
	 * Construct a JSONArray from a Collection.
	 *
	 * @param collection A Collection.
	 */
	JSONArray(Collection<?> collection) {
		if (collection == null) {
			this.myArrayList = new ArrayList<>();
		} else {
			this.myArrayList = new ArrayList<>(collection.size());
			for (Object o : collection) {
				this.myArrayList.add(JSONObject.wrap(o));
			}
		}
	}

	/**
	 * Construct a JSONArray from an array
	 *
	 * @throws JSONException If not an array.
	 */
	JSONArray(Object array) throws JSONException {
		this();
		if (array.getClass().isArray()) {
			int length = Array.getLength(array);
			this.myArrayList.ensureCapacity(length);
			for (int i = 0; i < length; i += 1) {
				this.put(JSONObject.wrap(Array.get(array, i)));
			}
		} else {
			throw new JSONException(
					"JSONArray initial value should be a string or collection or array.");
		}
	}

	@Override
	public Iterator<Object> iterator() {
		return this.myArrayList.iterator();
	}

	/**
	 * Get the object value associated with an index.
	 *
	 * @param index The index must be between 0 and length() - 1.
	 * @return An object value.
	 * @throws JSONException If there is no value for the index.
	 */
	public Object get(int index) throws JSONException {
		Object object = this.opt(index);
		if (object == null) {
			throw new JSONException("JSONArray[" + index + "] not found.");
		}
		return object;
	}

	/**
	 * Get the number of elements in the JSONArray, included nulls.
	 *
	 * @return The length (or size).
	 */
	public int length() {
		return this.myArrayList.size();
	}

	/**
	 * Get the optional object value associated with an index.
	 *
	 * @param index The index must be between 0 and length() - 1. If not, null is returned.
	 * @return An object value, or null if there is no object at that index.
	 */
	private Object opt(int index) {
		return (index < 0 || index >= this.length()) ? null : this.myArrayList
				.get(index);
	}


	/**
	 * Append an object value. This increases the array's length by one.
	 *
	 * @param value An object value. The value should be a Boolean, Double,
	 *              Integer, JSONArray, JSONObject, Long, or String, or the
	 *              JSONObject.NULL object.
	 * @return this.
	 */
	JSONArray put(Object value) {
		this.myArrayList.add(value);
		return this;
	}

	/**
	 * Make a pretty-printed JSON text of this JSONArray.
	 * <p>
	 * <p>If <code>indentFactor > 0</code> and the {@link JSONArray} has only
	 * one element, then the array will be output on a single line:
	 * <pre>{@code [1]}</pre>
	 *
	 * <p>If an array has 2 or more elements, then it will be output across
	 * multiple lines: <pre>{@code
	 * [
	 * 1,
	 * "value 2",
	 * 3
	 * ]
	 * }</pre>
	 * <p><b>
	 * Warning: This method assumes that the data structure is acyclical.
	 * </b>
	 *
	 * @return a printable, displayable, transmittable representation of the
	 * object, beginning with <code>[</code>&nbsp;<small>(left
	 * bracket)</small> and ending with <code>]</code>
	 * &nbsp;<small>(right bracket)</small>.
	 * @throws JSONException json exceptions
	 */
	@Override
	public String toString() {
		try {
			StringWriter sw = new StringWriter();
			synchronized (sw.getBuffer()) {
				return this.write(sw, 0, 0).toString();
			}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Write the contents of the JSONArray as JSON text to a writer.
	 * <p>
	 * <p>If <code>indentFactor > 0</code> and the {@link JSONArray} has only
	 * one element, then the array will be output on a single line:
	 * <pre>{@code [1]}</pre>
	 *
	 * <p>If an array has 2 or more elements, then it will be output across
	 * multiple lines: <pre>{@code
	 * [
	 * 1,
	 * "value 2",
	 * 3
	 * ]
	 * }</pre>
	 * <p><b>
	 * Warning: This method assumes that the data structure is acyclical.
	 * </b>
	 *
	 * @param writer       Writes the serialized JSON
	 * @param indentFactor The number of spaces to add to each level of indentation.
	 * @param indent       The indentation of the top level.
	 * @return The writer.
	 * @throws JSONException json exceptions
	 */
	Writer write(Writer writer, int indentFactor, int indent)
			throws JSONException {
		try {
			boolean commanate = false;
			int length = this.length();
			writer.write('[');

			if (length == 1) {
				try {
					JSONObject.writeValue(writer, this.myArrayList.get(0),
							indentFactor, indent);
				} catch (Exception e) {
					throw new JSONException("Unable to write JSONArray value at index: 0", e);
				}
			} else if (length != 0) {
				final int newindent = indent + indentFactor;

				for (int i = 0; i < length; i += 1) {
					if (commanate) {
						writer.write(',');
					}
					if (indentFactor > 0) {
						writer.write('\n');
					}
					JSONObject.indent(writer, newindent);
					try {
						JSONObject.writeValue(writer, this.myArrayList.get(i),
								indentFactor, newindent);
					} catch (Exception e) {
						throw new JSONException("Unable to write JSONArray value at index: " + i, e);
					}
					commanate = true;
				}
				if (indentFactor > 0) {
					writer.write('\n');
				}
				JSONObject.indent(writer, indent);
			}
			writer.write(']');
			return writer;
		} catch (IOException e) {
			throw new JSONException(e);
		}
	}

	/**
	 * Returns a java.util.List containing all of the elements in this array.
	 * If an element in the array is a JSONArray or JSONObject it will also
	 * be converted.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @return a java.util.List containing the elements of this array
	 */
	List<Object> toList() {
		List<Object> results = new ArrayList<>(this.myArrayList.size());
		for (Object element : this.myArrayList) {
			if (element == null || JSONObject.NULL.equals(element)) {
				results.add(null);
			} else if (element instanceof JSONArray) {
				results.add(((JSONArray) element).toList());
			} else if (element instanceof JSONObject) {
				results.add(((JSONObject) element).toMap());
			} else {
				results.add(element);
			}
		}
		return results;
	}
}
