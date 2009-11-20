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
package plugins.floghelper.ui.flog;

import freenet.client.async.ManifestElement;
import freenet.clients.http.filter.ContentFilter;
import freenet.clients.http.filter.FoundURICallback;
import freenet.clients.http.filter.UnsafeContentTypeException;
import freenet.keys.FreenetURI;
import freenet.support.Logger;
import freenet.support.io.ArrayBucket;
import freenet.support.io.NullBucketFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Vector;
import plugins.floghelper.contentsyntax.ContentSyntax;
import plugins.floghelper.data.Content;
import plugins.floghelper.data.DataFormatter;
import plugins.floghelper.data.Flog;

/**
 * Generate an index which can be used with the Library plugin.
 *
 * @author Artefact2
 */
public class IndexBuilder {
	/**
	 * Substores to generate, only one digit should be enough
	 * even for biggest flogs.
	 */
	public final static byte[] subStores = new byte[]{ 0x0, 0x1, 0x2,
		0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf};

	private final Flog flog;
	private final HashMap<String, Object> parsedData;
	private final Vector<String> pageIDs = new Vector<String>();
	private final HashMap<Byte, Vector<String>> ourWords = new HashMap<Byte, Vector<String>>();
	private String baseKey;

	public IndexBuilder(Flog flog, HashMap<String, Object> parsedData) {
		this.flog = flog;
		this.parsedData = parsedData;
		try {
			this.baseKey = this.flog.getRequestURI().toString();
		} catch (Exception ex) {
			Logger.error(this, "", ex);
		}

		for(byte b : subStores) {
			ourWords.put(b, new Vector<String>());
		}
		try {
			this.getWordsFromPages();
		} catch (URISyntaxException ex) {
			Logger.error(this, "", ex);
		} catch (UnsafeContentTypeException ex) {
			Logger.error(this, "", ex);
		} catch (IOException ex) {
			Logger.error(this, "", ex);
		}
	}

	public HashMap<String, String> getFullIndex() {
		// Generate main index.xml file
		// Generate index_*.xml subindexes
		// Fore more info about the format :
		// http://wiki.freenetproject.org/XMLSpider

		HashMap<String, String> index = new HashMap<String, String>();

		index.put("index.xml", this.getIndexIndex());
		
		for(byte sub : IndexBuilder.subStores) {
			index.put("index_" + Integer.toHexString(sub).toLowerCase() + ".xml", this.getSubIndex(sub));
		}

		return index;
	}

	public String getIndexIndex() {
		StringBuilder index = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
				"<main_index>\n" +
				"	<prefix value=\"1\"/>\n" +
				"	<header>\n" +
				"		<title>Index of " + DataFormatter.htmlSpecialChars(this.flog.getTitle()) + "</title>\n" +
				"		<owner>" + this.flog.getAuthorName() + "</owner>\n" +
				"	</header>\n" +
				"	<keywords>\n");
		for(byte i : IndexBuilder.subStores) {
			index.append("		<subIndex key=\"" + Integer.toHexString(i).toLowerCase() + "\"/>\n");
		}

		index.append("	</keywords>\n" +
				"</main_index>\n");

		return index.toString();
	}

	public String getSubIndex(Byte sub) {
		StringBuilder subIndex = new StringBuilder();

		subIndex.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
		subIndex.append("<sub_index>\n");
		subIndex.append("	<entries value=\"" + this.ourWords.get(sub).size() + "\"/>\n");
		subIndex.append("	<header>\n");
		subIndex.append("		<title>Index of " + DataFormatter.htmlSpecialChars(this.flog.getTitle()) + "</title>\n");
		subIndex.append("	</header>\n");
		subIndex.append("	<files>\n");
		for(int i = 0; i < this.pageIDs.size(); ++i) {
			final String pageName = this.pageIDs.elementAt(i);
			subIndex.append("		<file id=\"" + Integer.toString(i) + "\" key=\"" + this.baseKey + pageName + "\" title=\"" +
					DataFormatter.htmlSpecialChars(this.flog.getContentByID(pageName.replace("Content-", "").replace(".html", "")).getTitle()) + "\"/>\n");
		}
		subIndex.append("	</files>\n");
		subIndex.append("	<keywords>\n");

		for(String s : this.ourWords.get(sub)) {
			subIndex.append("		<word v=\"" + s + "\">\n");

			for(int i = 0; i < pageIDs.size(); ++i) {
				final String pageName = this.pageIDs.elementAt(i);
				try {
					final String positions = IndexBuilder.findAllWordPositions(DataFormatter.readStringFromStream(((ManifestElement) this.parsedData.get(pageName)).getData().getInputStream()), s);
					if(positions != null) {
						subIndex.append("			<file id=\"" + Integer.toString(i) + "\">" + positions + "</file>\n");
					}
				} catch (IOException ex) {
					Logger.error(this, "", ex);
				}
			}

			subIndex.append("		</word>\n");
		}

		subIndex.append("	</keywords>\n");
		subIndex.append("</sub_index>\n");

		return subIndex.toString();
	}

	private static String findAllWordPositions(String xhtml, String word) {
		xhtml = xhtml.toLowerCase();
		word = word.toLowerCase();
		if(!xhtml.contains(word)) return null;

		StringBuilder positions = new StringBuilder();
		int offset = -1;
		int next;
		while ((next = xhtml.indexOf(word, offset+1)) > offset) {
			offset = next;
			if(positions.length() > 0) positions.append(",");
			positions.append(offset);
		}

		return positions.toString();
	}

	private void getWordsFromPages() throws URISyntaxException, UnsafeContentTypeException, IOException {
		// Content-XXXXXXX.html pages only, because they contain all the contents
		// and their respective URIs won't change over time

		Vector<String> words = new Vector<String>();

		for (final Content content : new FlogFactory(flog).getContentsTreeMap(false).values()) {
			NullFilterCallback nullFC = new NullFilterCallback();
			ContentFilter.filter(new ArrayBucket(ContentSyntax.parseSomeString(content.getContent(),
					content.getContentSyntax()).getBytes("UTF-8")), new NullBucketFactory(),
					"text/html", new URI("http://whocares.co:12345/"), nullFC, null);
			final String cURI = "Content-" + content.getID() + ".html";
			this.pageIDs.add(cURI);
			words.addAll(nullFC.words);
		}

		for(String w : words) {
			final Vector<String> list = this.ourWords.get(Byte.valueOf(DataFormatter.getMD5(w).substring(0, 1), 16));
			if(!list.contains(w))
				list.add(w);
		}
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
			for(String word : arg0.toLowerCase().split(",|\\.|;|:|\\!|\n+|\\s+|\\[|\\]|\\(|\\)|\\{|\\}")) {
				if(word.length() < 3) continue;
				if(!words.contains(word))
					words.add(word);
			}
		}
	}
}
