/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.ui;

import freenet.pluginmanager.PluginStore;
import java.util.Random;
import plugins.floghelper.FlogHelper;

/**
 *
 * @author romain
 */
public class DataFormatter {

	public static final Random r = new Random();

	public static final String formatIntLength(int toFormat, int size, boolean isHex) {
		String str = isHex ? Integer.toHexString(toFormat) : Integer.toString(toFormat);
		while (str.length() < size) {
			str = "0" + str;
		}
		return str;
	}

	public static final String printStore(PluginStore e) {
		return printStore(e, 1);
	}

	private static final String printStore(PluginStore e, int recursionLevel) {
		assert (recursionLevel >= 1);

		// Avoid StackOverflowException when there are bugs...
		if(recursionLevel > 30) return "";

		StringBuilder toReturn = new StringBuilder();

		if (e == null) {
			return "";
		}

		if (recursionLevel == 1) {
			toReturn.append("PluginStore:\n");
		} else {
			for (int i = 1; i < recursionLevel; ++i) {
				toReturn.append("----");
			}
			toReturn.append("SubStore:\n");
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
				toReturn.append(printStore(e.subStores.get(s), recursionLevel + 1));
			}
		}

		return toReturn.toString();
	}

	private static final void writeStoreLine(String key, Object value, int recursionLevel, StringBuilder sb) {
		for (int i = 0; i < recursionLevel; ++i) {
			sb.append("----");
		}

		sb.append(key).append(": ").append(toString(value)).append("\n");
	}

	private static final void writeStoreLine(String key, Object[] value, int recursionLevel, StringBuilder sb) {
		for (int i = 0; i < recursionLevel; ++i) {
			sb.append("----");
		}

		sb.append(key).append(": ").append(toString(value)).append("\n");
	}

	public static final String toString(Object value) {
		String valueStr;
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
		} else {
			valueStr = value.toString();
		}

		return valueStr;
	}

	public static final String toString(Object[] values) {
		StringBuilder sb = new StringBuilder("{ ");
		for (Object val : values) {
			sb.append(toString(val)).append(", ");
		}

		return sb.delete(sb.length() - 3, sb.length() - 1).append(" }").toString();
	}

	public static final String indentString(String s, int recurseLevel) {
		StringBuilder sb = new StringBuilder();
		String[] lines = s.split("\n");

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

	public static final String getRandomID() {
		return getRandomID(7);
	}

	public static final String getRandomID(int length) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < length; ++i) {
			sb.append(Integer.toHexString(r.nextInt(16)));
		}

		return sb.toString().toUpperCase();
	}

	public static final String createUniqueFlogID() {
		return createSubStoreUniqueID(FlogHelper.getStore());
	}

	public static final String createSubStoreUniqueID(PluginStore store) {
		while (true) {
			String id = getRandomID();

			// Make sure we return a ID that isn't already used.
			if (store.subStores.get(id) == null) {
				return id;
			}
		}
	}
}
