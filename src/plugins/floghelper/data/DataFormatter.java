/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.data;

import freenet.pluginmanager.PluginStore;
import java.util.Random;
import plugins.floghelper.FlogHelper;

/**
 * This is mostly debugging code : printing PluginStores in a human-readable way, ...
 * 
 * @author Artefact2
 */
public class DataFormatter {

	public static final Random r = new Random();

	/**
	 * Format an int, adding zeroes until the result has a certain length.
	 * Example : formatIntLength(12, 4, false) -> "0012"
	 *
	 * @param toFormat integer to format/
	 * @param size Size of the string to return, eg. 3 with a size of 5 will return 00003.
	 * @param isHex If true, the string should be returned in hexadecimal format.
	 * @return Formatted integer.
	 */
	public static final String formatIntLength(final int toFormat, final int size, final boolean isHex) {
		String str = isHex ? Integer.toHexString(toFormat) : Integer.toString(toFormat);
		while (str.length() < size) {
			str = "0" + str;
		}
		return str;
	}

	/**
	 * Print a PluginStore in a human readable format.
	 *
	 * @param e PluginStore to print.
	 * @return Textual representation of the store readable by a human.
	 */
	public static final String printStore(final PluginStore e) {
		return printStore(e, null, 1);
	}

	/**
	 * Print a PluginStore in a human readable format.
	 *
	 * @param e PluginStore to print.
	 * @param name Name of the PluginStore, will be shown as SubStore&lt;Name&gt;
	 * @param recursionLevel Value of deeness of this SubStore.
	 * @return Textual representation of the store readable by a human.
	 */
	private static final String printStore(final PluginStore e, final String name, final int recursionLevel) {
		assert (recursionLevel >= 1);

		// Avoid StackOverflowException when there are bugs...
		if(recursionLevel > 30) return "";

		final StringBuilder toReturn = new StringBuilder();

		if (e == null) {
			return "";
		}

		if (recursionLevel == 1) {
			toReturn.append("PluginStore:\n");
		} else {
			for (int i = 1; i < recursionLevel; ++i) {
				toReturn.append("----");
			}
			toReturn.append("SubStore<" +  name + ">:\n");
		}

		if (e.booleans != null) {
			for (String s : e.booleans.keySet()) {
				writeStoreLine(s, toString(e.booleans.get(s)), recursionLevel, toReturn);
			}
		}
		if (e.booleansArrays != null) {
			for (String s : e.booleansArrays.keySet()) {
				writeStoreLine(s, toString(e.booleansArrays.get(s)), recursionLevel, toReturn);
			}
		}
		if (e.bytes != null) {
			for (String s : e.bytes.keySet()) {
				writeStoreLine(s, toString(e.bytes.get(s)), recursionLevel, toReturn);
			}
		}
		if (e.bytesArrays != null) {
			for (String s : e.bytesArrays.keySet()) {
				writeStoreLine(s, toString(e.bytesArrays.get(s)), recursionLevel, toReturn);
			}
		}
		if (e.integers != null) {
			for (String s : e.integers.keySet()) {
				writeStoreLine(s, toString(e.integers.get(s)), recursionLevel, toReturn);
			}
		}
		if (e.integersArrays != null) {
			for (String s : e.integersArrays.keySet()) {
				writeStoreLine(s, toString(e.integersArrays.get(s)), recursionLevel, toReturn);
			}
		}
		if (e.longs != null) {
			for (String s : e.longs.keySet()) {
				writeStoreLine(s, toString(e.longs.get(s)), recursionLevel, toReturn);
			}
		}
		if (e.longsArrays != null) {
			for (String s : e.longsArrays.keySet()) {
				writeStoreLine(s, toString(e.longsArrays.get(s)), recursionLevel, toReturn);
			}
		}
		if (e.shorts != null) {
			for (String s : e.shorts.keySet()) {
				writeStoreLine(s, toString(e.shorts.get(s)), recursionLevel, toReturn);
			}
		}
		if (e.shortsArrays != null) {
			for (String s : e.shortsArrays.keySet()) {
				writeStoreLine(s, toString(e.shortsArrays.get(s)), recursionLevel, toReturn);
			}
		}
		if (e.strings != null) {
			for (String s : e.strings.keySet()) {
				writeStoreLine(s, indentString(e.strings.get(s), recursionLevel), recursionLevel, toReturn);
			}
		}
		if (e.stringsArrays != null) {
			for (String s : e.stringsArrays.keySet()) {
				writeStoreLine(s, toString(e.stringsArrays.get(s)), recursionLevel, toReturn);
			}
		}

		if (e.subStores != null) {
			for (String s : e.subStores.keySet()) {
				toReturn.append(printStore(e.subStores.get(s), s, recursionLevel + 1));
			}
		}

		return toReturn.toString();
	}

	/**
	 * Write a line in a store print output.
	 *
	 * @param key Key of the mapped element.
	 * @param value Value of the element.
	 * @param recursionLevel Level of deepness of the element.
	 * @param sb StringBuilder to use.
	 */
	private static final void writeStoreLine(final String key, final Object value, final int recursionLevel, final StringBuilder sb) {
		for (int i = 0; i < recursionLevel; ++i) {
			sb.append("----");
		}

		sb.append(key).append(": ").append(toString(value)).append("\n");
	}

	/**
	 * Return a textual representation of an object.
	 * @param value Object to use.
	 * @return Textual representation.
	 */
	public static final String toString(final Object value) {
		final String valueStr;
		if (value == null) {
			valueStr = "";
		} else if (value instanceof Boolean) {
			valueStr = Boolean.toString((Boolean) value);
		} else if (value instanceof Byte) {
			valueStr = Byte.toString((Byte) value);
		} else if (value instanceof String) {
			if ("".equals(value)) {
				valueStr = "";
			} else {
				valueStr = (String) value;
			}
		} else if (value instanceof Integer) {
			valueStr = Integer.toString((Integer) value);
		} else if (value instanceof Long) {
			valueStr = Long.toString((Long) value);
		} else if (value instanceof Short) {
			valueStr = Short.toString((Short) value);
		} else if(value instanceof byte[]) {
			valueStr = toString((byte[]) value);
		} else if(value instanceof String[]) {
			valueStr = toString((String[]) value);
		} else {
			valueStr = value.toString();
		}

		return valueStr;
	}

	/**
	 * Identical as toString(Object), except that it returns something like
	 * { Value1, Value2, Value3, ... }
	 *
	 * @param values Array of objects to print.
	 * @return Textual representation.
	 */
	public static final String toString(final byte[] values) {
		if(values == null)
			return "";
		final StringBuilder sb = new StringBuilder("{ ");
		int i = 0;
		for (byte val : values) {
			++i;
			sb.append(Byte.toString(val)).append(", ");
			if(i > 30) {
				sb.append("... (" + (values.length - i) + " more)" + "   ");
				break;
			}
		}

		return sb.delete(sb.length() - 3, sb.length() - 2).append("}").toString();
	}

	public static final String toString(final String[] values) {
		if(values == null)
			return "";
		final StringBuilder sb = new StringBuilder("{ ");
		int i = 0;
		for (String val : values) {
			++i;
			sb.append(val).append(", ");
		}

		return sb.delete(sb.length() - 2, sb.length() - 1).append("}").toString();
	}

	/**
	 * Indent a String, should be called by PrintStore for multiline string values.
	 *
	 * @param s String to test
	 * @param recurseLevel Level of deepness of the containing SubStore.
	 * @return Indented string
	 */
	private static final String indentString(final String s, final int recurseLevel) {
		final StringBuilder sb = new StringBuilder();
		final String[] lines = s.split("\n");

		boolean isFirst = true;
		for (String line : lines) {
			if (isFirst) {
				isFirst = false;
			} else {
				for (int i = 0; i < recurseLevel; ++i) {
					sb.append("----");
				}
			}

			sb.append(line);
			sb.append("\n");
		}

		return sb.substring(0, sb.length() - 1);
	}

	/**
	 * Get a random ID, like AAB07C9.
	 *
	 * @return Random ID
	 */
	public static final String getRandomID() {
		return getRandomID(7);
	}

	public static final String getRandomID(final int length) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < length; ++i) {
			sb.append(Integer.toHexString(r.nextInt(16)));
		}

		return sb.toString().toUpperCase();
	}

	/**
	 * Get a random ID with a given length.
	 *
	 * @return Random ID
	 */
	public static final String createUniqueFlogID() {
		return createSubStoreUniqueID(FlogHelper.getStore());
	}

	/**
	 * Get a random ID that is not currently mapped to anything in a store.
	 *
	 * @param store Store to use.
	 * @return Random, unused ID.
	 */
	public static final String createSubStoreUniqueID(final PluginStore store) {
		while (true) {
			final String id = getRandomID();

			// Make sure we return a ID that isn't already used.
			if (store.subStores.get(id) == null) {
				return id;
			}
		}
	}

	/**
	 * Try to parse a long value in a string.
	 *
	 * @param value String to parse.
	 * @param defaultValue Value to return if it fails.
	 * @return Parsed long or defaultValue if the parsing failed.
	 */
	public static final long tryParseLong(String value, long defaultValue) {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
}
