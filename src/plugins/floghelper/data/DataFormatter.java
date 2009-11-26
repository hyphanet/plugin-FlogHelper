/* FlogHelper, Freenet plugin to create flogs
 * Copyright (C) 2009 Romain "Artefact2" Dalmaso
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package plugins.floghelper.data;

import freenet.pluginmanager.PluginStore;
import freenet.support.HTMLEncoder;
import freenet.support.HexUtil;
import freenet.support.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Random;
import java.util.SimpleTimeZone;
import plugins.floghelper.FlogHelper;

/**
 * This is mostly debugging code : printing PluginStores in a human-readable way, ...
 * 
 * @author Artefact2
 */
public class DataFormatter {

	/**
	 * RFC3339-compliant dates are used in Atom feeds.
	 */
	public static final SimpleDateFormat RFC3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", java.util.Locale.US);
	/**
	 * Our date format used in the flog (is UTC).
	 */
	public static final SimpleDateFormat DefaultDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
	/**
	 * Date format used in the web UI (is local, eg. CET, ...)
	 */
	public static final SimpleDateFormat LocalDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

	static {
		RFC3339.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
		DefaultDateFormatter.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
	}

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
		return makeUniqueID(store.subStores);
	}

	/**
	 * Even more general approach.
	 *
	 * @param map Map of anything to use.
	 * @return Unique ID.
	 */
	public static final String makeUniqueID(Map<String, ? extends Object> map) {
		String id = getRandomID();
		while(map.containsKey(id)) {
			id = getRandomID();
		}

		return id;
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

	/**
	 * Obfuscate a date, eg. 2009-10-15 20:15:59 -> 2009-10-15 00:00:00
	 * 
	 * @param d Date to obfuscate
	 * @return Simplified date with less precision.
	 */
	public static Date obfuscateDate(Date d) {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(d);
		c.set(GregorianCalendar.MILLISECOND, 0);
		c.set(GregorianCalendar.SECOND, 0);
		c.set(GregorianCalendar.MINUTE, 0);
		c.set(GregorianCalendar.HOUR, 0);

		return c.getTime();
	}

	/**
	 * Make a string safe for printing in a xHTML document : symbols like >, <, ", &
	 * are replaced with their xHTML counterpart (&lt;, &gt;, ...).
	 *
	 * @param s String to parse.
	 * @return String that can be safely printed in a xHTML document.
	 */
	public static String htmlSpecialChars(String s) {
		return HTMLEncoder.encode(s);
	}

	/**
	 * This should escape a string for regex use.
	 *
	 * @param s String to escape.
	 * @return Escaped string that might be safe to use in a regex.
	 */
	public static String makeRegexSafe(String s) {
		// FIXME that list is not exhaustive at all.
		return s.replace("\\", "\\\\").replace("[", "\\[").replace("]", "\\]")
				.replace("*", "\\*").replace(".", "\\.").replace("|", "\\|").replace("^", "\\^")
				.replace("?", "\\?").replace("(", "\\(").replace(")", "\\)").replace("+", "\\+");
	}

	/**
	 * Insert a string regularily in another string. Example : "abcdef", "X", 2
	 * will give abXcdXef
	 * @param str Main string
	 * @param whatToInsert String to insert
	 * @param period Period of the inserted string
	 * @return
	 */
	public static String insertIntoString(String str, String whatToInsert, int period) {
		StringBuilder sb = new StringBuilder();

		sb.append(str.substring(0, Math.min(str.length(), period)));
		int offset = period;
		while(offset < str.length()) {
			sb.append(whatToInsert);
			sb.append(str.substring(offset, Math.min(str.length(), offset + period)));
			offset += period;
		}

		return sb.toString();
	}

	/**
	 * Get the MD5 of a string as a string.
	 * 
	 * @param s String to hash.
	 * @return MD5 sum of the string.
	 */
	public static String getMD5(String s) {
		try {
			final byte[] md5bytes = MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8"));
			return HexUtil.bytesToHex(md5bytes);
		} catch (NoSuchAlgorithmException ex) {
			Logger.error(DataFormatter.class, "", ex);
			return "";
		} catch (UnsupportedEncodingException ex) {
			Logger.error(DataFormatter.class, "", ex);
			return "";
		}
	}

	/**
	 * Read an InputStream and return a String.
	 *
	 * @param e InputStream to read.
	 * @return String
	 * @throws IOException
	 */
	public static String readStringFromStream(InputStream e) throws IOException {
		BufferedReader bsr = new BufferedReader(new InputStreamReader(e, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		String buffer = null;
		while((buffer = bsr.readLine()) != null) {
			sb.append(buffer);
		}
		return sb.toString();
	}
}
