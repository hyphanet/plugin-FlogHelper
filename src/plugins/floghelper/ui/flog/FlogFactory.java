/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.ui.flog;

import freenet.pluginmanager.PluginStore;
import freenet.support.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.contentsyntax.ContentSyntax;

/**
 * TODO: Contents
 * TODO: Tags (Count contents & sort)
 * TODO: Index
 * TODO: Archives by date (paging)
 * TODO: -------- by tags (paging)
 * TODO: RSS2 & Atom feeds
 *
 * @author Artefact2
 */
public class FlogFactory {
	private final PluginStore flog;

	public static String getTemplate() {
		// TODO maybe make this user-editable
		StringBuffer sb = new StringBuffer();
		InputStream res = FlogHelper.class.getClassLoader().getResourceAsStream("plugins/floghelper/ui/flog/GlobalTemplate.html");

		BufferedReader br = new BufferedReader(new InputStreamReader(res));
		String buffer = "";
		
		try {
			while ((buffer = br.readLine()) != null) {
				sb.append(buffer).append("\n");
			}
		} catch (IOException ex) {
			Logger.error(FlogFactory.class, "IOException while reading the template !");
		}

		return sb.toString();
	}

	public FlogFactory(PluginStore flog) {
		this.flog = flog;
	}

	public HashMap<String, String> parseAllFlog() {
		HashMap<String, String> fileMap = new HashMap<String, String>();

		return fileMap;
	}

	public String getContentPage(String contentID) {
		PluginStore content = this.flog.subStores.get(contentID);

		String genPage = getTemplate();
		
		String syntax = content.strings.get("ContentSyntax");
		if (syntax == null) {
			syntax = "RawXHTML";
		}

		try {
			genPage = genPage.replace("{MainContent}", ((ContentSyntax) Class.forName("plugins.floghelper.contentsyntax." + syntax).newInstance()).parseSomeString(content.strings.get("Content")));
		} catch (InstantiationException ex) {
			Logger.error(this, "Cannot instanciate Content syntax " + content.strings.get("ContentSyntax"));
		} catch (IllegalAccessException ex) {
			Logger.error(this, "Cannot instanciate Content syntax " + content.strings.get("ContentSyntax"));
		} catch (ClassNotFoundException ex) {
			Logger.error(this, "Cannot instanciate Content syntax " + content.strings.get("ContentSyntax"));
		}

		return genPage;
	}
}
