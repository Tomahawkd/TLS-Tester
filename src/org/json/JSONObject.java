package org.json;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;

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
 * A JSONObject is an unordered collection of name/value pairs. Its external
 * form is a string wrapped in curly braces with colons between the names and
 * values, and commas between the values and names. The internal form is an
 * object having <code>get</code> and <code>opt</code> methods for accessing
 * the values by name, and <code>put</code> methods for adding or replacing
 * values by name. The values can be any of these types: <code>Boolean</code>,
 * <code>JSONArray</code>, <code>JSONObject</code>, <code>Number</code>,
 * <code>String</code>, or the <code>JSONObject.NULL</code> object. A
 * JSONObject constructor can be used to convert an external form JSON text
 * into an internal form whose values can be retrieved with the
 * <code>get</code> and <code>opt</code> methods, or to convert values into a
 * JSON text using the <code>put</code> and <code>toString</code> methods. A
 * <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object, which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you. The opt methods differ from the get methods in that they
 * do not throw. Instead, they return a specified value, such as null.
 * <p>
 * The <code>put</code> methods add or replace values in an object. For
 * example,
 * <p>
 * <pre>
 * myString = new JSONObject()
 *         .put(&quot;JSON&quot;, &quot;Hello, World!&quot;).toString();
 * </pre>
 * <p>
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * the JSON syntax rules. The constructors are more forgiving in the texts they
 * will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing brace.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a
 * quote or single quote, and if they do not contain leading or trailing
 * spaces, and if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , #</code> and if they do not look like numbers and
 * if they are not the reserved words <code>true</code>, <code>false</code>,
 * or <code>null</code>.</li>
 * </ul>
 *
 * @author JSON.org
 * @version 2016-08-15
 */
public class JSONObject {
	/**
	 * JSONObject.NULL is equivalent to the value that JavaScript calls null,
	 * whilst Java's null is equivalent to the value that JavaScript calls
	 * undefined.
	 */
	private static final class Null {

		/**
		 * There is only intended to be a single instance of the NULL object,
		 * so the clone method returns itself.
		 *
		 * @return NULL.
		 */
		@Override
		protected final Object clone() {
			return this;
		}

		/**
		 * A Null object is equal to the null value and to itself.
		 *
		 * @param object An object to test for nullness.
		 * @return true if the object parameter is the JSONObject.NULL object or
		 * null.
		 */
		@Override
		@SuppressWarnings("all")
		public boolean equals(Object object) {
			return object == null || object == this;
		}

		/**
		 * A Null object is equal to the null value and to itself.
		 *
		 * @return always returns 0.
		 */
		@Override
		public int hashCode() {
			return 0;
		}

		/**
		 * Get the "null" string value.
		 *
		 * @return The string "null".
		 */
		@Override
		public String toString() {
			return "null";
		}
	}

	/**
	 * The map where the JSONObject's properties are kept.
	 */
	private final LinkedHashMap<String, Object> map;

	/**
	 * It is sometimes more convenient and less ambiguous to have a
	 * <code>NULL</code> object than to use Java's <code>null</code> value.
	 * <code>JSONObject.NULL.equals(null)</code> returns <code>true</code>.
	 * <code>JSONObject.NULL.toString()</code> returns <code>"null"</code>.
	 */
	static final Object NULL = new Null();

	/**
	 * Construct an empty JSONObject.
	 */
	JSONObject() {
		// HashMap is used on purpose to ensure that elements are unordered by
		// the specification.
		// JSON tends to be a portable transfer format to allows the container
		// implementations to rearrange their items for a faster element
		// retrieval based on associative access.
		// Therefore, an implementation mustn't rely on the order of the item.
		this.map = new LinkedHashMap<>();
	}

	/**
	 * Construct a JSONObject from a JSONTokener.
	 *
	 * @param x A JSONTokener object containing the source string.
	 * @throws JSONException If there is a syntax error in the source string or a
	 *                       duplicated key.
	 */
	JSONObject(JSONTokener x) throws JSONException {
		this();
		char c;
		String key;

		if (x.nextClean() != '{') {
			throw x.syntaxError("A JSONObject text must begin with '{'");
		}
		for (; ; ) {
			c = x.nextClean();
			switch (c) {
				case 0:
					throw x.syntaxError("A JSONObject text must end with '}'");
				case '}':
					return;
				default:
					x.back();
					key = x.nextValue().toString();
			}

			// The key is followed by ':'.

			c = x.nextClean();
			if (c != ':') {
				throw x.syntaxError("Expected a ':' after a key");
			}

			// Use syntaxError(..) to include error location

			if (key != null) {
				// Check if key exists
				if (this.opt(key) != null) {
					// key already exists
					throw x.syntaxError("Duplicate key \"" + key + "\"");
				}
				// Only add value if non-null
				Object value = x.nextValue();
				if (value != null) {
					this.put(key, value);
				}
			}

			// Pairs are separated by ','.

			switch (x.nextClean()) {
				case ';':
				case ',':
					if (x.nextClean() == '}') {
						return;
					}
					x.back();
					break;
				case '}':
					return;
				default:
					throw x.syntaxError("Expected a ',' or '}'");
			}
		}
	}

	/**
	 * Construct a JSONObject from an Object using bean getters. It reflects on
	 * all of the public methods of the object. For each of the methods with no
	 * parameters and a name starting with <code>"get"</code> or
	 * <code>"is"</code> followed by an uppercase letter, the method is invoked,
	 * and a key and the value returned from the getter method are put into the
	 * new JSONObject.
	 * <p>
	 * The key is formed by removing the <code>"get"</code> or <code>"is"</code>
	 * prefix. If the second remaining character is not upper case, then the
	 * first character is converted to lower case.
	 * <p>
	 * For example, if an object has a method named <code>"getName"</code>, and
	 * if the result of calling <code>object.getName()</code> is
	 * <code>"Larry Fine"</code>, then the JSONObject will contain
	 * <code>"name": "Larry Fine"</code>.
	 * <p>
	 * Methods that return <code>void</code> as well as <code>static</code>
	 * methods are ignored.
	 *
	 * @param bean An object that has getter methods that should be used to make
	 *             a JSONObject.
	 */
	private JSONObject(Object bean) {
		this();
		this.populateMap(bean);
	}

	/**
	 * Construct a JSONObject from a source JSON text string. This is the most
	 * commonly used JSONObject constructor.
	 *
	 * @param source A string beginning with <code>{</code>&nbsp;<small>(left
	 *               brace)</small> and ending with <code>}</code>
	 *               &nbsp;<small>(right brace)</small>.
	 * @throws JSONException If there is a syntax error in the source string or a
	 *                       duplicated key.
	 */
	public JSONObject(String source) throws JSONException {
		this(new JSONTokener(source));
	}

	/**
	 * Accumulate values under a key. It is similar to the put method except
	 * that if there is already an object stored under the key then a JSONArray
	 * is stored under the key to hold all of the accumulated values. If there
	 * is already a JSONArray, then the new value is appended to it. In
	 * contrast, the put method replaces the previous value.
	 * <p>
	 * If only one value is accumulated that is not a JSONArray, then the result
	 * will be the same as using put. But if multiple values are accumulated,
	 * then the result will be like append.
	 *
	 * @param key   A key string.
	 * @param value An object to be accumulated under the key.
	 * @return this.
	 * @throws JSONException If the value is an invalid number or if the key is null.
	 */

	@SuppressWarnings("all")
	JSONObject accumulate(String key, Object value) throws JSONException {
		testValidity(value);
		Object object = this.opt(key);
		if (object == null) {
			this.put(key,
					value instanceof JSONArray ? new JSONArray().put(value)
							: value);
		} else if (object instanceof JSONArray) {
			((JSONArray) object).put(value);
		} else {
			this.put(key, new JSONArray().put(object).put(value));
		}
		return this;
	}

	/**
	 * Get the value object associated with a key.
	 *
	 * @param key A key string.
	 * @return The object associated with the key.
	 * @throws JSONException if the key is not found.
	 */
	public Object get(String key) throws JSONException {
		if (key == null) {
			throw new JSONException("Null key.");
		}
		Object object = this.opt(key);
		if (object == null) {
			throw new JSONException("JSONObject[" + quote(key) + "] not found.");
		}
		return object;
	}

	/**
	 * Get a set of entries of the JSONObject. These are raw values and may not
	 * match what is returned by the JSONObject get* and opt* functions. Modifying
	 * the returned EntrySet or the Entry objects contained therein will modify the
	 * backing JSONObject. This does not return a clone or a read-only view.
	 * <p>
	 * Use with caution.
	 *
	 * @return An Entry Set
	 * @see Map#entrySet()
	 */
	Set<Entry<String, Object>> entrySet() {
		return this.map.entrySet();
	}

	/**
	 * Get the number of keys stored in the JSONObject.
	 *
	 * @return The number of keys in the JSONObject.
	 */
	int length() {
		return this.map.size();
	}


	/**
	 * Get an enumeration of the keys of the JSONObject. Modifying this key Set will also
	 * modify the JSONObject. Use with caution.
	 *
	 * @see Set#iterator()
	 *
	 * @return An iterator of the keys.
	 */
	public Iterator<String> keys() {
		return this.map.keySet().iterator();
	}

	/**
	 * Produce a string from a Number.
	 *
	 * @param number A Number
	 * @return A String.
	 * @throws JSONException If n is a non-finite number.
	 */
	private static String numberToString(Number number) throws JSONException {
		if (number == null) {
			throw new JSONException("Null pointer");
		}
		testValidity(number);

		// Shave off trailing zeros and decimal point, if possible.

		String string = number.toString();
		if (string.indexOf('.') > 0 && string.indexOf('e') < 0
				&& string.indexOf('E') < 0) {
			while (string.endsWith("0")) {
				string = string.substring(0, string.length() - 1);
			}
			if (string.endsWith(".")) {
				string = string.substring(0, string.length() - 1);
			}
		}
		return string;
	}

	/**
	 * Get an optional value associated with a key.
	 *
	 * @param key A key string.
	 * @return An object which is the value, or null if there is no value.
	 */
	Object opt(String key) {
		return key == null ? null : this.map.get(key);
	}

	/**
	 * Populates the internal map of the JSONObject with the bean properties.
	 * The bean can not be recursive.
	 *
	 * @param bean the bean
	 * @see JSONObject#JSONObject(Object)
	 */
	private void populateMap(Object bean) {
		Class<?> klass = bean.getClass();

// If klass is a System class then set includeSuperClass to false.

		boolean includeSuperClass = klass.getClassLoader() != null;

		Method[] methods = includeSuperClass ? klass.getMethods() : klass
				.getDeclaredMethods();
		for (final Method method : methods) {
			final int modifiers = method.getModifiers();
			if (Modifier.isPublic(modifiers)
					&& !Modifier.isStatic(modifiers)
					&& method.getParameterTypes().length == 0
					&& !method.isBridge()
					&& method.getReturnType() != Void.TYPE) {
				final String name = method.getName();
				String key;
				if (name.startsWith("get")) {
					if ("getClass".equals(name) || "getDeclaringClass".equals(name)) {
						continue;
					}
					key = name.substring(3);
				} else if (name.startsWith("is")) {
					key = name.substring(2);
				} else {
					continue;
				}
				if (key.length() > 0
						&& Character.isUpperCase(key.charAt(0))) {
					if (key.length() == 1) {
						key = key.toLowerCase(Locale.ROOT);
					} else if (!Character.isUpperCase(key.charAt(1))) {
						key = key.substring(0, 1).toLowerCase(Locale.ROOT)
								+ key.substring(1);
					}

					try {
						final Object result = method.invoke(bean);
						if (result != null) {
							this.map.put(key, wrap(result));
							// we don't use the result anywhere outside of wrap
							// if it's a resource we should be sure to close it after calling toString
							if (result instanceof Closeable) {
								try {
									((Closeable) result).close();
								} catch (IOException ignore) {
								}
							}
						}
					} catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException ignore) {
					}
				}
			}
		}
	}

	/**
	 * Put a key/value pair in the JSONObject. If the value is null, then the
	 * key will be removed from the JSONObject if it is present.
	 *
	 * @param key   A key string.
	 * @param value An object which is the value. It should be of one of these
	 *              types: Boolean, Double, Integer, JSONArray, JSONObject, Long,
	 *              String, or the JSONObject.NULL object.
	 * @return this.
	 * @throws JSONException If the value is non-finite number or if the key is null.
	 */

	@SuppressWarnings("all")
	private JSONObject put(String key, Object value) throws JSONException {
		if (key == null) {
			throw new NullPointerException("Null key.");
		}
		if (value != null) {
			testValidity(value);
			this.map.put(key, value);
		} else {
			this.remove(key);
		}
		return this;
	}

	/**
	 * Produce a string in double quotes with backslash sequences in all the
	 * right places. A backslash will be inserted within </, producing <\/,
	 * allowing JSON text to be delivered in HTML. In JSON text, a string cannot
	 * contain a control character or an unescaped quote or backslash.
	 *
	 * @param string A String
	 * @return A String correctly formatted for insertion in a JSON text.
	 */
	private static String quote(String string) {
		StringWriter sw = new StringWriter();
		synchronized (sw.getBuffer()) {
			try {
				return quote(string, sw).toString();
			} catch (IOException ignored) {
				// will never happen - we are writing to a string writer
				return "";
			}
		}
	}

	private static Writer quote(String string, Writer w) throws IOException {
		if (string == null || string.length() == 0) {
			w.write("\"\"");
			return w;
		}

		char b;
		char c = 0;
		String hhhh;
		int i;
		int len = string.length();

		w.write('"');
		for (i = 0; i < len; i += 1) {
			b = c;
			c = string.charAt(i);
			switch (c) {
				case '\\':
				case '"':
					w.write('\\');
					w.write(c);
					break;
				case '/':
					if (b == '<') {
						w.write('\\');
					}
					w.write(c);
					break;
				case '\b':
					w.write("\\b");
					break;
				case '\t':
					w.write("\\t");
					break;
				case '\n':
					w.write("\\n");
					break;
				case '\f':
					w.write("\\f");
					break;
				case '\r':
					w.write("\\r");
					break;
				default:
					if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
							|| (c >= '\u2000' && c < '\u2100')) {
						w.write("\\u");
						hhhh = Integer.toHexString(c);
						w.write("0000", 0, 4 - hhhh.length());
						w.write(hhhh);
					} else {
						w.write(c);
					}
			}
		}
		w.write('"');
		return w;
	}

	/**
	 * Remove a name and its value, if present.
	 *
	 * @param key The name to be removed.
	 * @return The value that was associated with the name, or null if there was
	 * no value.
	 */

	@SuppressWarnings("all")
	private Object remove(String key) {
		return this.map.remove(key);
	}

	/**
	 * Tests if the value should be tried as a decimal. It makes no test if there are actual digits.
	 *
	 * @param val value to test
	 * @return true if the string is "-0" or if it contains '.', 'e', or 'E', false otherwise.
	 */
	private static boolean isDecimalNotation(final String val) {
		return val.indexOf('.') > -1 || val.indexOf('e') > -1
				|| val.indexOf('E') > -1 || "-0".equals(val);
	}

	/**
	 * Try to convert a string into a number, boolean, or null. If the string
	 * can't be converted, return the string.
	 *
	 * @param string A String.
	 * @return A simple JSON value.
	 */
	static Object stringToValue(String string) {
		if (string.equals("")) {
			return string;
		}
		if (string.equalsIgnoreCase("true")) {
			return Boolean.TRUE;
		}
		if (string.equalsIgnoreCase("false")) {
			return Boolean.FALSE;
		}
		if (string.equalsIgnoreCase("null")) {
			return JSONObject.NULL;
		}

        /*
         * If it might be a number, try converting it. If a number cannot be
         * produced, then the value will just be a string.
         */

		char initial = string.charAt(0);
		if ((initial >= '0' && initial <= '9') || initial == '-') {
			try {
				// if we want full Big Number support this block can be replaced with:
				// return stringToNumber(string);
				if (isDecimalNotation(string)) {
					Double d = Double.valueOf(string);
					if (!d.isInfinite() && !d.isNaN()) {
						return d;
					}
				} else {
					Long myLong = Long.valueOf(string);
					if (string.equals(myLong.toString())) {
						if (myLong == myLong.intValue()) {
							return myLong.intValue();
						}
						return myLong;
					}
				}
			} catch (Exception ignore) {
			}
		}
		return string;
	}

	/**
	 * Throw an exception if the object is a NaN or infinite number.
	 *
	 * @param o The object to test.
	 * @throws JSONException If o is a non-finite number.
	 */
	private static void testValidity(Object o) throws JSONException {
		if (o != null) {
			if (o instanceof Double) {
				if (((Double) o).isInfinite() || ((Double) o).isNaN()) {
					throw new JSONException(
							"JSON does not allow non-finite numbers.");
				}
			} else if (o instanceof Float) {
				if (((Float) o).isInfinite() || ((Float) o).isNaN()) {
					throw new JSONException(
							"JSON does not allow non-finite numbers.");
				}
			}
		}
	}

	/**
	 * Make a pretty-printed JSON text of this JSONObject.
	 * <p>
	 * <p>If <code>indentFactor > 0</code> and the {@link JSONObject}
	 * has only one key, then the object will be output on a single line:
	 * <pre>{@code {"key": 1}}</pre>
	 *
	 * <p>If an object has 2 or more keys, then it will be output across
	 * multiple lines: <code><pre>{
	 *  "key1": 1,
	 *  "key2": "value 2",
	 *  "key3": 3
	 * }</pre></code>
	 * <p><b>
	 * Warning: This method assumes that the data structure is acyclical.
	 * </b>
	 *
	 * @return a printable, displayable, portable, transmittable representation
	 * of the object, beginning with <code>{</code>&nbsp;<small>(left
	 * brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 * brace)</small>.
	 * @throws JSONException If the object contains an invalid number.
	 */

	@Override
	public String toString() {
		try {
			StringWriter w = new StringWriter();
			synchronized (w.getBuffer()) {
				return this.write(w, 0, 0).toString();
			}
		}catch (Exception e) {
			return null;
		}
	}

	/**
	 * Wrap an object, if necessary. If the object is null, return the NULL
	 * object. If it is an array or collection, wrap it in a JSONArray. If it is
	 * a map, wrap it in a JSONObject. If it is a standard property (Double,
	 * String, et al) then it is already wrapped. Otherwise, if it comes from
	 * one of the java packages, turn it into a string. And if it doesn't, try
	 * to wrap it in a JSONObject. If the wrapping fails, then null is returned.
	 *
	 * @param object The object to wrap
	 * @return The wrapped value
	 */
	static Object wrap(Object object) {
		try {
			if (object == null) {
				return NULL;
			}
			if (object instanceof JSONObject || object instanceof JSONArray
					|| NULL.equals(object) || object instanceof JSONString
					|| object instanceof Byte || object instanceof Character
					|| object instanceof Short || object instanceof Integer
					|| object instanceof Long || object instanceof Boolean
					|| object instanceof Float || object instanceof Double
					|| object instanceof String || object instanceof BigInteger
					|| object instanceof BigDecimal || object instanceof Enum) {
				return object;
			}

			if (object instanceof Collection) {
				Collection<?> coll = (Collection<?>) object;
				return new JSONArray(coll);
			}
			if (object.getClass().isArray()) {
				return new JSONArray(object);
			}
			if (object instanceof Map) {
				Map<?, ?> map = (Map<?, ?>) object;
				return new JSONObject(map);
			}
			Package objectPackage = object.getClass().getPackage();
			String objectPackageName = objectPackage != null ? objectPackage
					.getName() : "";
			if (objectPackageName.startsWith("java.")
					|| objectPackageName.startsWith("javax.")
					|| object.getClass().getClassLoader() == null) {
				return object.toString();
			}
			return new JSONObject(object);
		} catch (Exception exception) {
			return null;
		}
	}

	@SuppressWarnings("all")
	static Writer writeValue(Writer writer, Object value,
	                         int indentFactor, int indent) throws JSONException, IOException {
		if (value == null || value.equals(null)) {
			writer.write("null");
		} else if (value instanceof JSONString) {
			Object o;
			try {
				o = ((JSONString) value).toJSONString();
			} catch (Exception e) {
				throw new JSONException(e);
			}
			writer.write(o != null ? o.toString() : quote(value.toString()));
		} else if (value instanceof Number) {
			// not all Numbers may match actual JSON Numbers. i.e. fractions or Imaginary
			final String numberAsString = numberToString((Number) value);
			try {
				// Use the BigDecimal constructor for it's parser to validate the format.
				@SuppressWarnings("unused")
				BigDecimal testNum = new BigDecimal(numberAsString);
				// Close enough to a JSON number that we will use it unquoted
				writer.write(numberAsString);
			} catch (NumberFormatException ex) {
				// The Number value is not a valid JSON number.
				// Instead we will quote it as a string
				quote(numberAsString, writer);
			}
		} else if (value instanceof Boolean) {
			writer.write(value.toString());
		} else if (value instanceof Enum<?>) {
			writer.write(quote(((Enum<?>) value).name()));
		} else if (value instanceof JSONObject) {
			((JSONObject) value).write(writer, indentFactor, indent);
		} else if (value instanceof JSONArray) {
			((JSONArray) value).write(writer, indentFactor, indent);
		} else if (value instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) value;
			new JSONObject(map).write(writer, indentFactor, indent);
		} else if (value instanceof Collection) {
			Collection<?> coll = (Collection<?>) value;
			new JSONArray(coll).write(writer, indentFactor, indent);
		} else if (value.getClass().isArray()) {
			new JSONArray(value).write(writer, indentFactor, indent);
		} else {
			quote(value.toString(), writer);
		}
		return writer;
	}

	static void indent(Writer writer, int indent) throws IOException {
		for (int i = 0; i < indent; i += 1) {
			writer.write(' ');
		}
	}

	/**
	 * Write the contents of the JSONObject as JSON text to a writer.
	 * <p>
	 * <p>If <code>indentFactor > 0</code> and the {@link JSONObject}
	 * has only one key, then the object will be output on a single line:
	 * <pre>{@code {"key": 1}}</pre>
	 *
	 * <p>If an object has 2 or more keys, then it will be output across
	 * multiple lines: <code><pre>{
	 *  "key1": 1,
	 *  "key2": "value 2",
	 *  "key3": 3
	 * }</pre></code>
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
	private Writer write(Writer writer, int indentFactor, int indent)
			throws JSONException {
		try {
			boolean commanate = false;
			final int length = this.length();
			writer.write('{');

			if (length == 1) {
				final Entry<String, ?> entry = this.entrySet().iterator().next();
				final String key = entry.getKey();
				writer.write(quote(key));
				writer.write(':');
				if (indentFactor > 0) {
					writer.write(' ');
				}
				try {
					writeValue(writer, entry.getValue(), indentFactor, indent);
				} catch (Exception e) {
					throw new JSONException("Unable to write JSONObject value for key: " + key, e);
				}
			} else if (length != 0) {
				final int newindent = indent + indentFactor;
				for (final Entry<String, ?> entry : this.entrySet()) {
					if (commanate) {
						writer.write(',');
					}
					if (indentFactor > 0) {
						writer.write('\n');
					}
					indent(writer, newindent);
					final String key = entry.getKey();
					writer.write(quote(key));
					writer.write(':');
					if (indentFactor > 0) {
						writer.write(' ');
					}
					try {
						writeValue(writer, entry.getValue(), indentFactor, newindent);
					} catch (Exception e) {
						throw new JSONException("Unable to write JSONObject value for key: " + key, e);
					}
					commanate = true;
				}
				if (indentFactor > 0) {
					writer.write('\n');
				}
				indent(writer, indent);
			}
			writer.write('}');
			return writer;
		} catch (IOException exception) {
			throw new JSONException(exception);
		}
	}

	/**
	 * Returns a java.util.Map containing all of the entries in this object.
	 * If an entry in the object is a JSONArray or JSONObject it will also
	 * be converted.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @return a java.util.Map containing the entries of this object
	 */
	Map<String, Object> toMap() {
		Map<String, Object> results = new LinkedHashMap<>();
		for (Entry<String, Object> entry : this.entrySet()) {
			Object value;
			if (entry.getValue() == null || NULL.equals(entry.getValue())) {
				value = null;
			} else if (entry.getValue() instanceof JSONObject) {
				value = ((JSONObject) entry.getValue()).toMap();
			} else if (entry.getValue() instanceof JSONArray) {
				value = ((JSONArray) entry.getValue()).toList();
			} else {
				value = entry.getValue();
			}
			results.put(entry.getKey(), value);
		}
		return results;
	}
}
