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

import freenet.client.filter.ContentFilter;
import freenet.client.filter.FoundURICallback;
import freenet.client.filter.UnsafeContentTypeException;
import freenet.keys.FreenetURI;
import freenet.support.HTMLEncoder;
import freenet.support.Logger;
import freenet.support.io.ArrayBucket;
import freenet.support.io.NullBucket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	private final Vector<String> pageIDs = new Vector<String>();
	private final HashMap<String, HashMap<Integer, Vector<Long>>> ourWords = new HashMap<String, HashMap<Integer, Vector<Long>>>();
	private final HashMap<Byte, Vector<String>> wordsByMD5 = new HashMap<Byte, Vector<String>>();
	private String baseKey;

	public IndexBuilder(Flog flog) {
		this.flog = flog;
		try {
			this.baseKey = this.flog.getRequestURI().toString();
		} catch (Exception ex) {
			Logger.error(this, "", ex);
		}

		for(byte b : subStores) {
			this.wordsByMD5.put(b, new Vector<String>());
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

		// Word -> (FileID, Positions)
		// FileID -> Filename
		// FirstMD5 -> Words

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
				"		<title>Index of " + HTMLEncoder.encodeXML(this.flog.getTitle()) + "</title>\n" +
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
		subIndex.append("	<entries value=\"" + this.wordsByMD5.get(sub).size() + "\"/>\n");
		subIndex.append("	<header>\n");
		subIndex.append("		<title>Index of " + HTMLEncoder.encodeXML(this.flog.getTitle()) + "</title>\n");
		subIndex.append("	</header>\n");
		subIndex.append("	<files>\n");
		for(int i = 0; i < this.pageIDs.size(); ++i) {
			final String pageName = this.pageIDs.elementAt(i);
			subIndex.append("		<file id=\"" + Integer.toString(i) + "\" key=\"" + this.baseKey + pageName + "\" title=\"" +
					HTMLEncoder.encodeXML(this.flog.getContentByID(pageName.replace("Content-", "").replace(".html", "")).getTitle()) + "\"/>\n");
		}
		subIndex.append("	</files>\n");
		subIndex.append("	<keywords>\n");

		for(String w : this.wordsByMD5.get(sub)) {
			subIndex.append("		<word v=\"" + HTMLEncoder.encodeXML(w) + "\">\n");

			for(int i = 0; i < pageIDs.size(); ++i) {
				if(!this.ourWords.get(w).containsKey(i)) {
					continue;
				}
				StringBuilder positions = new StringBuilder();
				boolean first = true;
				for(Long pos : this.ourWords.get(w).get(i)) {
					if(first) {
						first = false;
					}
					else {
						positions.append(",");
					}
					positions.append(Long.toString(pos));
				}
				subIndex.append("			<file id=\"" + Integer.toString(i) + "\">" + positions.toString() + "</file>\n");
			}

			subIndex.append("		</word>\n");
		}

		subIndex.append("	</keywords>\n");
		subIndex.append("</sub_index>\n");

		return subIndex.toString();
	}

	private void getWordsFromPages() throws URISyntaxException, UnsafeContentTypeException, IOException {
		// Content-XXXXXXX.html pages only, because they contain all the contents
		// and their respective URIs won't change over time
		InputStream filterInput = null;
		OutputStream filterOutput = null;
		ArrayBucket input = null;
		NullBucket output = null;
		for (final Content content : new FlogFactory(flog).getContentsTreeMap(false).values()) {
			NullFilterCallback nullFC = new NullFilterCallback();
			input = new ArrayBucket(ContentSyntax.parseSomeString(content.getContent(),
					content.getContentSyntax()).getBytes("UTF-8"));
			output = new NullBucket();
			filterInput = input.getInputStream();
			filterOutput = output.getOutputStream();
			ContentFilter.filter(filterInput, filterOutput,
					"text/html", new URI("http://whocares.co:12345/"), nullFC, null, null);
			filterInput.close();
			filterOutput.close();
			input.free();
			output.free();
			final String cURI = "Content-" + content.getID() + ".html";
			this.pageIDs.add(cURI);
			final int pageID = this.pageIDs.indexOf(cURI);
			for(String w : nullFC.words.keySet()) {
				if(!this.ourWords.containsKey(w)) {
					this.ourWords.put(w, new HashMap<Integer, Vector<Long>>());
				}
				final HashMap<Integer, Vector<Long>> container = this.ourWords.get(w);
				if(!container.containsKey(pageID)) {
					container.put(pageID, new Vector<Long>());
				}
				final Vector<Long> subContainer = container.get(pageID);
				for(Long position : nullFC.words.get(w)) {
					if(!subContainer.contains(position)) {
						subContainer.add(position);
					}
				}

				final Byte firstMD5 = Byte.valueOf(DataFormatter.getMD5(w).substring(0, 1), 16);
				final Vector<String> md5Container = this.wordsByMD5.get(firstMD5);
				if(!md5Container.contains(w)) {
					md5Container.add(w);
				}
			}
		}
	}

	private class NullFilterCallback implements FoundURICallback {

		private final HashMap<String, Vector<Long>> words = new HashMap<String, Vector<Long>>();
		private long position = 0;

		public void foundURI(FreenetURI arg0) {
			// Doesn't matter
		}

		public void foundURI(FreenetURI arg0, boolean arg1) {
			// Doesn't matter
		}

		public void onText(String arg0, String arg1, URI arg2) {
			for(String word : arg0.toLowerCase().split(",|\\.|;|:|\\!|\n+|\\s+|\\[|\\]|\\(|\\)|\\{|\\}")) {
				if(word.length() < 3) continue;
				if(!words.containsKey(word)) {
					words.put(word, new Vector<Long>());
				}
				words.get(word).add(position);

				this.position++;
			}
		}

		@Override
		public void onFinishedPage() {
			// Doesn't matter
		}
	}
}
