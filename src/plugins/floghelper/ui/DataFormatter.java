/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins.floghelper.ui;

import java.util.concurrent.ConcurrentHashMap;
import plugins.floghelper.data.FlogContainer;

/**
 *
 * @author romain
 */
public class DataFormatter {
	public static final String formatIntLength(int toFormat, int size, boolean isHex) {
		String str = isHex ? Integer.toHexString(toFormat) : Integer.toString(toFormat);
		while(str.length() < size) {
			str = "0" + str;
		}
		return str;
	}

	public static final String printFlogContainer(FlogContainer e) {
		return printFlogContainer(e.data, 0);
	}

	public static final String printFlogContainer(ConcurrentHashMap e, int marginLevel) {
		String toReturn = "";

		for (int i = 0; i < e.size(); ++i) {
			Object o = e.get(i);

			for (int j = 0; j < marginLevel; ++j) {
				toReturn += "-";
			}

			if (o == null) {
				toReturn += "(null)";
			} else if (o instanceof ConcurrentHashMap) {
				toReturn += "ConcurrentHashMap:\n;";
				toReturn += printFlogContainer((ConcurrentHashMap) o, marginLevel + 4);
			} else if (o instanceof Boolean) {
				toReturn += Boolean.toString((Boolean) o);
			} else if (o instanceof Long) {
				toReturn += Long.toString((Long) o);
			} else if (o instanceof Integer) {
				toReturn += Integer.toString((Integer) o);
			} else {
				toReturn += o.toString();
			}

			toReturn += "\n";
		}

		return toReturn;
	}
}

