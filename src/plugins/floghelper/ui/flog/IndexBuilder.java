/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.ui.flog;

import freenet.clients.http.filter.ContentFilter;
import freenet.clients.http.filter.FoundURICallback;
import freenet.clients.http.filter.UnsafeContentTypeException;
import freenet.keys.FreenetURI;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginStore;
import freenet.support.Logger;
import freenet.support.io.ArrayBucket;
import freenet.support.io.NullBucketFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Vector;
import plugins.floghelper.contentsyntax.ContentSyntax;
import plugins.floghelper.fcp.wot.WoTOwnIdentities;

/**
 * Generate an index which can be used with the Library plugin.
 *
 * @author Artefact2
 */
public class IndexBuilder {
	private final PluginStore flog;
	private final HashMap<String, Object> parsedData;
	private String baseKey;

	public IndexBuilder(PluginStore flog, HashMap<String, Object> parsedData) {
		this.flog = flog;
		this.parsedData = parsedData;
		try {
			this.baseKey = "USK@" + WoTOwnIdentities.getWoTIdentities("RequestURI").get(this.flog.strings.get("Author")).split("@")[1].split("/")[0] + "/" + flog.strings.get("SSKPath") + "/-1/";
		} catch (PluginNotFoundException ex) {
			// Safe to ignore.
		}
	}

	public HashMap<String, Object> getFullIndex() {
		// Generate main index.xml file
		// Generate index_*.xml subindexes
		// http://wiki.freenetproject.org/XMLSpider

		throw new UnsupportedOperationException();
	}

	private HashMap<String, Vector<Integer>> findAllWordPositions(String xhtml, String[] words) {
		HashMap<String, Vector<Integer>> wordPosition = new HashMap<String, Vector<Integer>>();

		for(String word : words) {
			Vector<Integer> positions = new Vector<Integer>();
			int offset = 0;
			int next;
			while((next = xhtml.indexOf(word, offset)) > offset) {
				offset = next;
				positions.add(offset);
			}
		}

		return wordPosition;
	}

	public HashMap<String, Vector<String>> getWordsFromPages() throws URISyntaxException, UnsafeContentTypeException, IOException {
		// Content-XXXXXXX.html pages only, because they contain all the contents
		// and their respective URIs won't change over time

		HashMap<String, Vector<String>> wordsInContents = new HashMap<String, Vector<String>>();

		NullFilterCallback nullFC = new NullFilterCallback();
		for (final PluginStore content : new FlogFactory(flog).getContentsTreeMap(false).values()) {
			ContentFilter.filter(new ArrayBucket(ContentSyntax.parseSomeString(content.strings.get("Content"), 
					content.strings.get("ContentSyntax")).getBytes("UTF-8")), new NullBucketFactory(),
					"text/html", new URI("http://whocares.co:12345/"), nullFC, null);
			wordsInContents.put("Content-" + content.strings.get("ID") + ".html", nullFC.words);
		}

		return wordsInContents;
	}

	private class NullFilterCallback implements FoundURICallback {

		private final Vector<String> words = new Vector<String>();

		public void foundURI(FreenetURI arg0) {
			// Doesn't matter
		}

		public void foundURI(FreenetURI arg0, boolean arg1) {
			// Doesn't matter
		}

		public void onText(String arg0, String arg1, URI arg2) {
			for(String word : arg0.toLowerCase().split(",|\\.|;|:|\\!|\n+|\\s+")) {
				words.add(word);
			}
		}
	}
}
