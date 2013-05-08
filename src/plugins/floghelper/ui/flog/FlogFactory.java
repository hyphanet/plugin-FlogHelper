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

import com.db4o.ObjectContainer;
import freenet.client.DefaultMIMETypes;
import freenet.client.async.ClientContext;
import freenet.client.async.DBJob;
import freenet.client.async.DatabaseDisabledException;
import freenet.client.async.ManifestElement;
import freenet.client.async.TooManyFilesInsertException;
import freenet.keys.FreenetURI;
import freenet.node.RequestStarter;
import freenet.node.fcp.ClientPutDir;
import freenet.node.fcp.ClientRequest;
import freenet.node.fcp.FCPClient;
import freenet.node.fcp.FCPServer;
import freenet.node.fcp.IdentifierCollisionException;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.support.Logger;
import freenet.support.api.Bucket;
import freenet.support.io.BucketTools;
import freenet.support.io.NativeThread;
import freenet.support.io.PersistentTempBucketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.Version;
import plugins.floghelper.contentsyntax.ContentSyntax;
import plugins.floghelper.data.Attachment;
import plugins.floghelper.data.Content;
import plugins.floghelper.data.DataFormatter;
import plugins.floghelper.data.Flog;
import plugins.floghelper.fcp.wot.WoTContexts;

/**
 * Flog parsing, generates xHTML code and various other flog-related things.
 *
 * @author Artefact2
 */
public class FlogFactory {
	/**
	 * List of availible flog themes.
	 */
	public static String[] THEMES = new String[]{
		"WhiteAndGray", // The first here is the default one.
		"VeryMinimalist-Light",
		"VeryMinimalist-Dark"
	};

	/**
	 * List of primary navigation links.
	 */
	private Vector<String[]> primaryNavigationLinks = new Vector<String[]>();

	private final Flog flog;

	/**
	 * Get a string resource from its path in the jar file.
	 *
	 * @param resource Resource to get.
	 * @return Text Resource
	 */
	private static String getResourceAsString(String resource) {
		StringBuffer sb = new StringBuffer();
		InputStream res = FlogHelper.class.getClassLoader().getResourceAsStream(resource);

		if(res == null) {
			// Don't die because of a NPE
			Logger.error(FlogFactory.class, "Got a null resource : " + resource);
			return "";
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(res));
		String buffer = "";

		try {
			while ((buffer = br.readLine()) != null) {
				sb.append(buffer).append("\n");
			}
		} catch (IOException ex) {
			Logger.error(FlogFactory.class, "IOException while reading resource " + resource + " !");
		}

		return sb.toString();
	}

	/**
	 * Get the default xHTML Template of the flog's template if the user override it.
	 *
	 * @return xHTML template
	 */
	public String getTemplate() {
		if(flog.overrideTemplate()) {
			return flog.getTemplateOverride();
		} else return getResourceAsString("plugins/floghelper/ui/flog/GlobalTemplate.html");
	}

	/**
	 * Get the default CSS sheet of the flog's sheet if the user override it.
	 *
	 * @return CSS sheet
	 */
	public String getCSS() {
		if(flog.overrideCSS()) {
			return flog.getCSSOverride();
		} else {
			return getResourceAsString("plugins/floghelper/ui/flog/" + flog.getTheme() + ".css");
		}

	}

	/**
	 * Create a new FlogFactory.
	 *
	 * @param flog Flog to use.
	 */
	public FlogFactory(Flog flog) {
		this.flog = flog;

		this.primaryNavigationLinks.add(new String[]{flog.getBaseL10n().getString("Flog.Index"), "./index.html"});
		this.primaryNavigationLinks.add(new String[]{flog.getBaseL10n().getString("Flog.Archives"), "./Archives-p1.html"});
		this.primaryNavigationLinks.add(new String[]{flog.getBaseL10n().getString("Flog.AtomFeed"), "./AtomFeed.xml"});
		try {
			this.primaryNavigationLinks.add(new String[]{flog.getBaseL10n().getString("Flog.BookmarkThisFlog"), "/?newbookmark=" + flog.getRequestURI() + "&amp;desc=" + this.flog.getTitle()});
		} catch (Exception ex) {
				Logger.error(this, "", ex);
		}

		if(flog.shouldPublishLibraryIndex()) {
			try {
				this.primaryNavigationLinks.add(new String[]{"<form enctype=\"multipart/form-data\" action=\"/library/\" method=\"post\" accept-charset=\"utf-8\"><p style=\"margin:0;\">" +
						"<input name=\"search\" type=\"text\" size=\"8\"/>" +
						"<input name=\"index0\" type=\"hidden\" value=\"" + flog.getRequestURI() + "index.xml\" />" +
						"<input name=\"extraindexcount\" type=\"hidden\" value=\"1\" />" +
						"<input type=\"submit\" value=\"" + flog.getBaseL10n().getString("Flog.Search") + "\"/></p></form>", null});
			} catch (Exception ex) {
				Logger.error(this, "", ex);
			}
		}
	}

	/**
	 * Get the parsed list of primary navigation links.
	 *
	 * @param currentUri Actual relative URI (eg. like /Content-...)
	 * @return Parsed xHTML code, only <li>s and </li>s (eg. no <ul>)
	 */
	public String getPrimaryNavigationLinks(String currentUri) {
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < this.primaryNavigationLinks.size(); ++i) {
			boolean thereIsALink = this.primaryNavigationLinks.elementAt(i)[1] != null;
			// We don't want to cause a NPE, yet we don't want a link if we already are on the page.
			if(thereIsALink) thereIsALink = ! (currentUri != null && this.primaryNavigationLinks.elementAt(i)[1].endsWith(currentUri));

			sb.append("<li>");
			if(thereIsALink) {
				sb.append("<a href=\"").append(this.primaryNavigationLinks.elementAt(i)[1]).append("\">");
			}
			sb.append(this.primaryNavigationLinks.elementAt(i)[0]);
			if(thereIsALink) {
				sb.append("</a>");
			}
			sb.append("</li>");

			sb.append("\n\t\t\t\t\t");
		}

		return sb.toString();
	}

	/**
	 * A very simple class to represent tags.
	 */
	private class Tag {
		public static final int S_NORMAL = 0;
		public static final int S_MINOR = -1;
		public static final int S_MAJOR = 1;
		/**
		 * The name of the tag.
		 */
		public String name;
		/**
		 * The number of articles having this tag in the flog we are
		 * currently parsing.
		 */
		public long articleCount;
		/**
		 * Status of the tag : minor, normal, major
		 */
		public int status;
	}

	/**
	 * Get a parsed tag list. Works the same as getPrimaryNavigationLinks.
	 * @param currentUri Actual relative URI
	 * @return Parsed xHTML code.
	 */
	public String getTagList(String currentUri) {
		HashMap<String, Tag> tags = new HashMap<String, Tag>();
		for(Content content : this.flog.getContents()) {
			if(content.isDraft()) continue;
			for(String tag : content.getTags()) {
				if(tags.containsKey(tag)) {
					Tag t =  tags.get(tag);
					t.articleCount += 1L;
				} else {
					final Tag newTag = new Tag();
					newTag.articleCount = 1L;
					newTag.name = tag;
					tags.put(tag, newTag);
				}
			}
		}

		Comparator<Tag> compareTags = new Comparator<Tag>() {
			public int compare(Tag t, Tag t1) {
				if(t.articleCount < t1.articleCount) return -1;
				else if(t.articleCount > t1.articleCount) return 1;
				else return -t.name.compareToIgnoreCase(t1.name);
			}
		};

		// Sort tags by count, and compute quintiles
		TreeSet<Tag> sortedTags = new TreeSet<Tag>(compareTags);
		sortedTags.addAll(tags.values());
		long i = 0;
		Long majorLimit = 0L, minorLimit = 0L;
		boolean majorSet = false, minorSet = false;
		for(Tag t : sortedTags.descendingSet()) {
			if(majorSet) {
				majorLimit += t.articleCount;
				majorLimit /= 2;
				majorSet = false;
			}
			if(minorSet) {
				minorLimit += t.articleCount;
				minorLimit /= 2;
				minorSet = false;
			}

			if(majorLimit != null && minorLimit != null) break;

			if(i <= 0.25 * sortedTags.size() && (i+1) >= 0.25 * sortedTags.size()) {
				// First quintile
				majorLimit = t.articleCount;
				majorSet = true;
			} else if(i <= 0.75 * sortedTags.size() && (i+1) >= 0.75 * sortedTags.size()) {
				// Last quintile
				minorLimit = t.articleCount;
				minorSet = true;
			}
			++i;
		}

		for(Tag t : sortedTags.descendingSet()) {
			if(t.articleCount <= minorLimit) {
				t.status = Tag.S_MINOR;
			} else if(t.articleCount >= majorLimit) {
				t.status = Tag.S_MAJOR;
			} else t.status = Tag.S_NORMAL;
		}

		// Resort tags by alph. order if needed
		if(!this.flog.shouldSortTagsByCount()) {
			compareTags = new Comparator<Tag>() {
				public int compare(Tag t, Tag t1) {
					return -t.name.compareToIgnoreCase(t1.name);
				}
			};
			sortedTags = new TreeSet<Tag>(compareTags);
			sortedTags.addAll(tags.values());
		}

		StringBuilder sb = new StringBuilder();
		if(!tags.isEmpty()) {
			sb.append("<ul>\n");
			for(Tag t : sortedTags.descendingSet()) {
				if(t.name.trim().equals("")) continue;
				boolean thereIsALink = ! (currentUri != null && currentUri.equals("/Tag-" + t.name + "-p1.html"));

				sb.append("<li>");
				if(t.status == Tag.S_MAJOR) {
					sb.append("<strong>");
				} else if(t.status == Tag.S_MINOR) {
					sb.append("<small>");
				}
				if(thereIsALink) {
					sb.append("<a href=\"./Tag-").append(t.name).append("-p1.html\">");
				}
				sb.append(DataFormatter.htmlSpecialChars(t.name));
				if(thereIsALink) {
					sb.append("</a>");
				}
				sb.append(" (").append(Long.toString(t.articleCount)).append(")");
				if(t.status == Tag.S_MAJOR) {
					sb.append("</strong>");
				} else if(t.status == Tag.S_MINOR) {
					sb.append("</small>");
				}
				sb.append("</li>");
				sb.append("\n\t\t\t\t\t");
			}
			sb.append("</ul>\n");
		}

		return sb.toString();
	}

	/**
	 * Get a parsed content block, containing a header and a content div.
	 *
	 * @param content Content to parse.
	 * @return xHTML code.
	 */
	private String getParsedContentBlock(Content content) {
		final StringBuilder mainContent = new StringBuilder();
		String syntax = content.getContentSyntax();

		mainContent.append("<div class=\"content_container\" id=\"c" + content.getID() + "\">");
		mainContent.append("<div class=\"content_header\">");
		mainContent.append("<h1>").append(DataFormatter.htmlSpecialChars(content.getTitle())).append("</h1><p>");
		mainContent.append("<a href=\"./Content-").append(content.getID()).append(".html\">" + flog.getBaseL10n().getString("Flog.PermanentLink") + "</a> ");
		//mainContent.append("| <a href=\"./Content-").append(content.strings.get("ID")).append(".html#comments\">Comments</a> ");
		mainContent.append("| " + flog.getBaseL10n().getString("Flog.Tags2") + " ");
		boolean first = true;
		for (String tag : content.getTags()) {
			if (tag.trim().equals("")) {
				continue;
			}
			if (first) {
				first = false;
			} else {
				mainContent.append(", ");
			}
			mainContent.append("<a href=\"./Tag-").append(tag).append("-p1.html\">").append(DataFormatter.htmlSpecialChars(tag)).append("</a>");
		}

		if (first) {
			mainContent.append("<em>" + flog.getBaseL10n().getString("Flog.TagsNone") + "</em>");
		}

		if (flog.shouldPublishDates()) {
			if(Math.abs(content.getContentCreationDate().getTime() - content.getContentModificationDate().getTime()) > 10000) {
				mainContent.append("<br /><small>" + flog.getBaseL10n().getString("Flog.LastModification") + " " +
						DataFormatter.DefaultDateFormatter.format(content.getContentModificationDate())).append("</small>");
			}
			mainContent.append("<br /><small>" + flog.getBaseL10n().getString("Flog.CreationDate") + " ").append(
					DataFormatter.DefaultDateFormatter.format(content.getContentCreationDate())).append("</small>");
		}
		mainContent.append("</p></div><div class=\"content_content\">").
				append(ContentSyntax.parseSomeString(content.getContent(), syntax)).append("</div></div>");

		return mainContent.toString();
	}

	/**
	 * Parse all values that won't change from one page to another.
	 *
	 * @param template Template to parse.
	 * @param uri Actual relative uri.
	 * @return Partially parsed template.
	 */
	private String parseInvariantData(String template, String uri) {
		String authorName = flog.getAuthorName();

		template = template.replace("{AuthorName}", authorName);
		template = template.replace("{AuthorNameWithLineBreaks}", DataFormatter.insertIntoString(authorName, "<br />", 18));
		template = template.replace("{FlogName}", DataFormatter.htmlSpecialChars(this.flog.getTitle()));
		template = template.replace("{FlogLang}", flog.getLang().shortCode);
		template = template.replace("{FlogDescription}", flog.getShortDescription());
		template = template.replace("{StyleURI}", "./GlobalStyle.css");
		template = template.replace("{AtomFeedURI}", "./AtomFeed.xml");
		template = template.replace("{AdditionalMenuContent}", ""); // FIXME that might have a use later
		template = template.replace("{FooterContent}", "<!-- Generated by FlogHelper " + Version.getVersion() + " -->");
		template = template.replace("{PrimaryNavigationLinks}", getPrimaryNavigationLinks(uri));
		template = template.replace("{TagsLinks}", this.getTagList(uri));
		template = template.replace("{ActivelinkIfAny}", this.flog.hasActivelink() ? "<p style=\"text-align: center;\"><img src=\"activelink.png\" alt=\"Activelink\" /></p>" : "");
		template = template.replace("{l10n:AboutTheAuthor}", flog.getBaseL10n().getString("Flog.AboutTheAuthor"));
		template = template.replace("{l10n:Navigation}", flog.getBaseL10n().getString("Flog.Navigation"));
		template = template.replace("{l10n:Tags}", flog.getBaseL10n().getString("Flog.Tags"));

		return template;
	}

	/**
	 * Parse all the flog and return a map of generated pages.
	 *
	 * @return All the flog pages, fully parsed, in xHTML.
	 */
	public HashMap<String, Object> parseAllFlog() throws IOException {
		final HashMap<String, Object> fileMap = new HashMap<String, Object>();
		Bucket data; String name;
		final PersistentTempBucketFactory factory = FlogHelper.getPR().getNode().clientCore.persistentTempBucketFactory;

		data = BucketTools.makeImmutableBucket(factory, this.getIndex().getBytes("UTF-8"));
		name = "index.html";
		fileMap.put(name, new ManifestElement(name, data, "text/html", data.size()));

		data = BucketTools.makeImmutableBucket(factory, this.getCSS().getBytes("UTF-8"));
		name = "GlobalStyle.css";
		fileMap.put(name, new ManifestElement(name, data, "text/css", data.size()));

		TreeMap<Long, Content> contents = this.getContentsTreeMap(false);
		HashMap<String, Long> tagCount = new HashMap<String, Long>();

		for(Content c : contents.values()) {
			data = BucketTools.makeImmutableBucket(factory, this.getContentPage(c.getID()).getBytes("UTF-8"));
			name = "Content-" + c.getID() + ".html";
			fileMap.put(name, new ManifestElement(name, data, "text/html", data.size()));

			for(String tag : c.getTags()) {
				tagCount.put(tag, tagCount.containsKey(tag) ? tagCount.get(tag) + 1 : 1);
			}
		}

		if(!contents.isEmpty()) {
			data = BucketTools.makeImmutableBucket(factory, this.getAtomFeed().getBytes("UTF-8"));
			name = "AtomFeed.xml";
			fileMap.put(name, new ManifestElement(name, data, "application/atom+xml", data.size()));
		}

		final long contentsPerArchivesPage = flog.getNumberOfContentsOnArchives();

		for(String tag : tagCount.keySet()) {
			final long pageMax = (long)Math.ceil((double)tagCount.get(tag) / (double)contentsPerArchivesPage);
			for(long i = 1; i <= pageMax; ++i) {
				data = BucketTools.makeImmutableBucket(factory, this.getTagsPage(tag, i).getBytes("UTF-8"));
				name = "Tag-" + tag + "-p" + Long.toString(i) + ".html";
				fileMap.put(name, new ManifestElement(name, data, "text/html", data.size()));
			}
		}

		final long pageMax = (long)Math.ceil((double)contents.size() / (double)contentsPerArchivesPage);
		for(long i = 1; i <= pageMax; ++i) {
				data = BucketTools.makeImmutableBucket(factory, this.getArchives(i).getBytes("UTF-8"));
				name = "Archives-p" + Long.toString(i) + ".html";
				fileMap.put(name, new ManifestElement(name, data, "text/html", data.size()));
		}

		if(flog.hasActivelink()) {
			data = BucketTools.makeImmutableBucket(factory, flog.getActivelink());
			name = "activelink.png";
			fileMap.put(name, new ManifestElement(name, data, "image/png", data.size()));
		}

		for(Attachment attachment : flog.getAttachments()) {
			data = BucketTools.makeImmutableBucket(factory, attachment.getData());
			name = attachment.getName();
			fileMap.put(name, new ManifestElement(name, data, DefaultMIMETypes.guessMIMEType(name, true), data.size()));
		}

		if(flog.shouldPublishLibraryIndex()) {
			HashMap<String, String> indexes = new IndexBuilder(this.flog).getFullIndex();
			for(String file : indexes.keySet()) {
				data = BucketTools.makeImmutableBucket(factory, indexes.get(file).getBytes("UTF-8"));
				fileMap.put(file, new ManifestElement(file, data, DefaultMIMETypes.guessMIMEType(file, true), data.size()));
			}
		}

		// shouldPublishStoreDump is disabled.
		
		return fileMap;
	}

	/**
	 * Insert the flog using a global, persistent insert.
	 *
	 * @throws IOException
	 * @throws PluginNotFoundException
	 * @throws DatabaseDisabledException
	 */
	public void insert() throws IOException, PluginNotFoundException, DatabaseDisabledException {
		final FCPServer fcp = FlogHelper.getPR().getNode().clientCore.getFCPServer();
		final HashMap<String, Object> parsedFlog = this.parseAllFlog();
		final FCPClient client = fcp.getGlobalForeverClient();
		final FreenetURI uri;
		FreenetURI temp;
		try {
			temp = flog.getInsertURI();
		} catch (Exception ex) {
			Logger.error(this, "", ex);
			temp = null;
		}
		
		// This is tricky but it works, we need uri to be final.
		uri = temp;

		FlogHelper.getPR().getNode().clientCore.queue(new DBJob() {

			public boolean run(ObjectContainer arg0, ClientContext arg1) {
				try {
					/**
					 * FCPClient client,
					 * FreenetURI uri,
					 * String identifier,
					 * int verbosity,
					 * short priorityClass,
					 * short persistenceType,
					 * String clientToken,
					 * boolean getCHKOnly,
					 * boolean dontCompress,
					 * int maxRetries,
					 * HashMap<String, Object> elements,
					 * String defaultName,
					 * boolean global,
					 * boolean earlyEncode,
					 * boolean canWriteClientCache,
					 * boolean forkOnCacheable,
					 * int extraInsertsSingleBlock,
					 * int extraInsertsSplitfileHeaderBlock
					 * boolean realTimeFlag
					 * byte[] overrideSplitfileCryptoKey
					 * FCPServer server,
					 * ObjectContainer container
					 */
					ClientPutDir cpd = new ClientPutDir(client, uri, "FlogHelper: " + flog.getTitle() + " (" + flog.getID() + DataFormatter.getRandomID(4) + ")", Integer.MAX_VALUE, RequestStarter.IMMEDIATE_SPLITFILE_PRIORITY_CLASS, ClientRequest.PERSIST_FOREVER, null, false, false, -1, parsedFlog, "index.html", true, false, false, true, 2, 2, false, null, fcp, arg0);
					try {
						fcp.startBlocking(cpd, arg0, arg1);
						WoTContexts.addContext(flog.getAuthorID());
					} catch (Exception ex) {
						Logger.error(this, "",  ex);
					}
				} catch (IdentifierCollisionException ex) {
					Logger.error(this, "",  ex);
				} catch (MalformedURLException ex) {
					Logger.error(this, "",  ex);
				} catch (TooManyFilesInsertException ex) {
					Logger.error(this, "",  ex);
				}
				return true;
			}
		}, NativeThread.MAX_PRIORITY, true);
	}

	/**
	 * Get a parsed /Content-CONTENTID.html page.
	 *
	 * @param contentID ContentID to use.
	 * @return Parsed xHTML page.
	 */
	public String getContentPage(String contentID) {
		final String draftWarning;
		Content content = this.flog.getContentByID(contentID);

		String genPage = this.parseInvariantData(getTemplate(), null);
		genPage = genPage.replace("{PageTitle}", DataFormatter.htmlSpecialChars(content.getTitle()));

		if(content.isDraft()) {
			draftWarning = "<p style=\"margin-top: 5px; padding: 5px; border: 1px solid red;\">"
					+ FlogHelper.getBaseL10n().getString("PreviewingDraftContent") + "</p>";
		} else draftWarning = "";

		return genPage.replace("{MainContent}", draftWarning + "<div id=\"singlecontent\">" + this.getParsedContentBlock(content) + "</div>");
	}

	/**
	 * Get a parsed /index.html page.
	 *
	 * @return Parsed xHTML page.
	 */
	public String getIndex() {
		String genPage = this.parseInvariantData(getTemplate(), "/index.html");
		genPage = genPage.replace("{PageTitle}", "Index");

		final TreeMap<Long, Content> contents = this.getContentsTreeMap(false);
		final Long numberOfContentsToShow = this.flog.getNumberOfContentsOnIndex();

		StringBuilder mainContent = new StringBuilder();
		for (int i = 0; i < numberOfContentsToShow; ++i) {
			if(contents.isEmpty()) break;

			final Content content = contents.get(contents.lastKey());
			contents.remove(content.getContentCreationDate().getTime());

			mainContent.append(this.getParsedContentBlock(content));
		}

		return genPage.replace("{MainContent}", mainContent.toString());
	}

	/**
	 * Get a parsed /Archives-pPAGE.html
	 *
	 * @param page Page number (from 1 to ceil(numberOfContents/contentsPerPage))
	 * @return Parsed xHTML page
	 */
	public String getArchives(long page) {
		String genPage = this.parseInvariantData(getTemplate(), "/Archives-p" + Long.toString(page) + ".html");
		genPage = genPage.replace("{PageTitle}", flog.getBaseL10n().getString("Flog.ArchivesPageN", "N", Long.toString(page)));

		final TreeMap<Long, Content> contents = this.getContentsTreeMap(false);
		final Long numberOfContentsToShow = this.flog.getNumberOfContentsOnArchives();
		final long numberOfContents = contents.size();

		for(int i = 0; i < (page-1)*numberOfContentsToShow; ++i) {
			final Content content = contents.get(contents.lastKey());
			contents.remove(content.getContentCreationDate().getTime());
		}

		StringBuilder mainContent = new StringBuilder();
		for (int i = 0; i < numberOfContentsToShow; ++i) {
			if(contents.isEmpty()) break;

			final Content content = contents.get(contents.lastKey());
			contents.remove(content.getContentCreationDate().getTime());

			mainContent.append(this.getParsedContentBlock(content));
		}

		final String pages = "<p class=\"pagination\">" + flog.getBaseL10n().getString("Flog.Pages") + " " + this.makePagination(numberOfContents, numberOfContentsToShow, page, "./Archives-p{Page}.html") + "</p>";

		return genPage.replace("{MainContent}", pages + mainContent.toString() + pages);
	}

	/**
	 * Get a parsed /Tag-TAGNAME-pPAGE.html
	 * Works the same as getArchives().
	 *
	 * @param tag Tag name to use
	 * @param page Page number
	 * @return Parsed xHTML page.
	 */
	public String getTagsPage(String tag, long page) {
		String genPage = this.parseInvariantData(getTemplate(), "/Tag-" + tag + "-p" + Long.toString(page) + ".html");
		genPage = genPage.replace("{PageTitle}", flog.getBaseL10n().getString("Flog.ArchivesTaggedWithTagPageN", new String[]{"Tag", "N"}, new String[]{DataFormatter.htmlSpecialChars(tag), Long.toString(page)}));

		final TreeMap<Long, Content> contents = this.getContentsTreeMapFilteredByTag(tag, false);
		final Long numberOfContentsToShow = this.flog.getNumberOfContentsOnArchives();
		final long numberOfContents = contents.size();

		for(int i = 0; i < (page-1)*numberOfContentsToShow; ++i) {
			final Content content = contents.get(contents.lastKey());
			contents.remove(content.getContentCreationDate().getTime());
		}

		StringBuilder mainContent = new StringBuilder();
		for (int i = 0; i < numberOfContentsToShow; ++i) {
			if(contents.isEmpty()) break;

			final Content content = contents.get(contents.lastKey());
			contents.remove(content.getContentCreationDate().getTime());

			mainContent.append(this.getParsedContentBlock(content));
		}

		final String pages = "<p class=\"pagination\">" + flog.getBaseL10n().getString("Flog.Pages") + " " + this.makePagination(numberOfContents, numberOfContentsToShow, page, "./Tag-" + tag + "-p{Page}.html") + "</p>";

		return genPage.replace("{MainContent}", pages + mainContent.toString() + pages);
	}

	/**
	 * Get the Atom feed of contents.
	 * @return Valid Atom 1.0 feed content (XML).
	 */
	public String getAtomFeed() {
		StringBuilder feed = new StringBuilder();
		feed.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		feed.append("<feed xmlns=\"http://www.w3.org/2005/Atom\">\n");
		feed.append("	<id>tag:freenet-");
		feed.append(this.flog.getID()).append("-").append(DataFormatter.htmlSpecialChars(this.flog.getAuthorID()));
		feed.append("</id>\n");

		feed.append("	<title type=\"html\">");
		feed.append(DataFormatter.htmlSpecialChars(this.flog.getTitle()));
		feed.append("</title>\n");

		TreeMap<Long, Content> contents = this.getContentsTreeMap(false);
		if(contents.size() == 0) return "";

		Date mostRecentlyCreationDate = new Date(contents.lastKey());
		if(!flog.shouldPublishDates()) {
			mostRecentlyCreationDate = DataFormatter.obfuscateDate(mostRecentlyCreationDate);
		}

		feed.append("	<updated>");
		feed.append(DataFormatter.RFC3339.format(mostRecentlyCreationDate));
		feed.append("</updated>\n");

		String author = flog.getAuthorName();

		feed.append("	<author><name>");
		feed.append(DataFormatter.htmlSpecialChars(author));
		feed.append("</name></author>\n");

		feed.append("	<link rel=\"self\" href=\"./AtomFeed.xml\" />\n");
		feed.append("	<generator version=\"").append(Version.getVersion()).append("\">FlogHelper</generator>\n");

		feed.append("	<subtitle type=\"xhtml\">\n" +
				"		<div xmlns=\"http://www.w3.org/1999/xhtml\">\n");
		feed.append("<p>").append(flog.getShortDescription()).append("</p>");
		feed.append("\n		</div>\n" +
				"	</subtitle>\n");

		Long[] l = contents.keySet().toArray(new Long[contents.size()]);
		for(int i=l.length-1;i>=0;i--) {
			Long creationDate = l[i];
			Content content = contents.get(creationDate);
			feed.append("	<entry>\n");
			feed.append("		<id>tag:freenet-");
			feed.append(this.flog.getID()).append("-").append(content.getID()).append("-").append(DataFormatter.htmlSpecialChars(this.flog.getAuthorID()));
			feed.append("</id>\n");

			feed.append("		<title type=\"text\">");
			feed.append(content.getTitle());
			feed.append("</title>\n");

			Date modifDate = content.getContentModificationDate();
			Date creaDate = content.getContentCreationDate();

			if(!flog.shouldPublishDates()) {
				modifDate = DataFormatter.obfuscateDate(modifDate);
				creaDate = DataFormatter.obfuscateDate(creaDate);
			}

			feed.append("	<published>");
			feed.append(DataFormatter.RFC3339.format(creaDate));
			feed.append("</published>\n");
			feed.append("	<updated>");
			feed.append(DataFormatter.RFC3339.format(modifDate));
			feed.append("</updated>\n");
			feed.append("		<author><name>");
			feed.append(author);
			feed.append("</name></author>\n");
			feed.append("		<content type=\"xhtml\">\n" +
					"			<div xmlns=\"http://www.w3.org/1999/xhtml\">\n");
			feed.append(ContentSyntax.parseSomeString(content.getContent(),
					content.getContentSyntax()));
			feed.append("\n			</div>\n" +
					"		</content>\n");
			feed.append("		<link rel=\"alternate\" href=\"./Content-").append(content.getID()).append(".html\" />\n");
			feed.append("</entry>\n");
		}


		return feed.append("</feed>").toString();
	}

	/**
	 * Get all the contents, sorted by ascending creation date.
	 *
	 * @return Tree of all the contents.
	 */
	public TreeMap<Long, Content> getContentsTreeMap(boolean includeDrafts) {
		TreeMap<Long, Content> map = new TreeMap<Long, Content>();

		for(Content c : this.flog.getContents()) {
			if(!includeDrafts && c.isDraft()) continue;
			map.put(c.getContentCreationDate().getTime(), c);
		}

		return map;
	}

	/**
	 * Get all the contents tagged with a particular tag, sorted by ascending creation date.
	 *
	 * @param tag Tag to match.
	 * @return Tree of contents tagged with the specified tag.
	 */
	private TreeMap<Long, Content> getContentsTreeMapFilteredByTag(String tag, boolean includeDrafts) {
		TreeMap<Long, Content> map = new TreeMap<Long, Content>();

		for (Content c : this.flog.getContents()) {
			if (!includeDrafts && c.isDraft()) continue;
			boolean isTagInside = c.getTags().contains(tag);

			if (isTagInside) {
				map.put(c.getContentCreationDate().getTime(), c);
			}
		}

		return map;
	}

	/**
	 * Get the pagination links.
	 * Example : < Previous | Next > | 1 2 3 ... 7 8 9 10 11 ... 52 53 54
	 *
	 * @param numberOfElements Total number of elements
	 * @param elementsPerPage Number of elements on a page
	 * @param page Current page number
	 * @param pageUri URIs of the pages, {Page} will be replaced by the good page number accordingly.
	 * @return Parsed xHTML code of the pages, no <p>.
	 */
	private String makePagination(long numberOfElements, long elementsPerPage, long page, String pageUri) {
		final long pageMax = (long)Math.ceil((double)numberOfElements / (double)elementsPerPage);
		final long pageMin = 1;

		StringBuilder sb = new StringBuilder();

		if(page <= pageMin) {
			sb.append("&lt; Previous");
		} else sb.append("<a href=\"").append(pageUri.replace("{Page}", Long.toString(page - 1L))).append("\">&lt; " + flog.getBaseL10n().getString("Flog.PreviousPage") + "</a>");
		sb.append(" | ");
		if(page >= pageMax) {
			sb.append("Next &gt;");
		} else sb.append("<a href=\"").append(pageUri.replace("{Page}", Long.toString(page + 1L))).append("\">" + flog.getBaseL10n().getString("Flog.NextPage") + " &gt;</a>");
		sb.append(" | ");

		for(long i = pageMin; i <= pageMax; ++i) {
			if(Math.abs(i - pageMin) < 5L || Math.abs(i - pageMax) < 5L || Math.abs(i - page) < 5L) {
				if(i == page) {
					sb.append(Long.toString(i) + " ");
				} else {
					sb.append("<a href=\"").append(pageUri.replace("{Page}", Long.toString(i))).append("\">").append(i).append("</a> ");
				}
			} else {
				sb.append("\t");
			}
		}

		return sb.toString().replaceAll("\t+", " ... ");
	}
}
