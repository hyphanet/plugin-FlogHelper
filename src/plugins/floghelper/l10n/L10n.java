/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.l10n;

import freenet.l10n.L10n.LANGUAGE;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Artefact2
 */
public class L10n {
	private static LANGUAGE lang = null;
	private static ResourceBundle res = null;

	public static String getString(final String key) {
		if(res == null) {
			L10n.setLanguage(LANGUAGE.getDefault());
		}
		return res.getString(key);
		// FIXME
	}

	public static void setLanguage(final LANGUAGE lang) {
		L10n.lang = lang;
		// FIXME
		final String langFile = "/plugins/floghelper/l10n/UI";
		L10n.res = ResourceBundle.getBundle(langFile, new Locale(L10n.lang.shortCode));
	}
}
