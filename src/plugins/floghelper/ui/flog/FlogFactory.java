/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.ui.flog;

import freenet.pluginmanager.PluginStore;
import java.util.HashMap;

/**
 * Contents
 * Tags (Count contents & sort)
 * Index
 * Archives by date (paging)
 * -------- by tags (paging)
 * RSS2 & Atom feeds
 *
 * @author Artefact2
 */
public class FlogFactory {
	private final PluginStore flog;

	public FlogFactory(PluginStore flog) {
		this.flog = flog;
	}

	public HashMap<String, String> parseAllFlog() {
		HashMap<String, String> fileMap = new HashMap<String, String>();

		return fileMap;
	}
}
