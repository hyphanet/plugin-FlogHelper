/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.ui.flog;

import com.db4o.ObjectContainer;
import freenet.client.DefaultMIMETypes;
import freenet.client.async.ClientContext;
import freenet.client.async.DBJob;
import freenet.client.async.DatabaseDisabledException;
import freenet.client.async.ManifestElement;
import freenet.keys.FreenetURI;
import freenet.node.RequestStarter;
import freenet.node.fcp.ClientPutDir;
import freenet.node.fcp.ClientRequest;
import freenet.node.fcp.FCPClient;
import freenet.node.fcp.FCPServer;
import freenet.node.fcp.IdentifierCollisionException;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginStore;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.contentsyntax.ContentSyntax;
import plugins.floghelper.data.DataFormatter;
import plugins.floghelper.fcp.wot.WoTOwnIdentities;

/**
 * Flog parsing, generates xHTML code and various other flog-related things.
 *
 * @author Artefact2
 */
public class FlogFactory {
	/**
	 * List of primary navigation links.
	 */
	private String[] primaryNavigationLinks;

	/**
	 * By default, we won't publish creation/modification dates of flogs/contents.
	 */
	public static boolean DEFAULT_SHOULD_PUBLISH_DATES = false;

	/**
	 * If we don't publish dates, we don't publish a dump of the store, because it
	 * contains the dates.
	 */
	public static final boolean DEFAULT_SHOULD_INSERT_STOREDUMP = false;

	/**
	 * Seven seems reasonable.
	 */
	public static final long DEFAULT_CONTENTS_ON_INDEX = 7;

	/**
	 * We don't want to insert an insane number of pages, so this number
	 * should be high enough.
	 */
	public static final long DEFAULT_CONTENTS_ON_ARCHIVES = 25;

	/**
	 * USK@crypto/__?__/revnumber
	 */
	public static final String DEFAULT_SSK_PATH = "flog";

	private final PluginStore flog;

	/**
	 * Get a string resource from its path in the jar file.
	 *
	 * @param resource Resource to get.
	 * @return Text Resource
	 */
	private static String getResourceAsString(String resource) {
		StringBuffer sb = new StringBuffer();
		InputStream res = FlogHelper.class.getClassLoader().getResourceAsStream(resource);

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
		if(flog.booleans.get("OverrideTemplate") != null && flog.booleans.get("OverrideTemplate") == true) {
			return flog.strings.get("OverrideTemplateValue");
		} else return getResourceAsString("plugins/floghelper/ui/flog/GlobalTemplate.html");
	}

	/**
	 * Get the default CSS sheet of the flog's sheet if the user override it.
	 *
	 * @return CSS sheet
	 */
	public String getCSS() {
		if(flog.booleans.get("OverrideCSS") != null && flog.booleans.get("OverrideCSS") == true) {
			return flog.strings.get("OverrideCSSValue");
		} else return getResourceAsString("plugins/floghelper/ui/flog/GlobalStyle.css");

	}

	/**
	 * Create a new FlogFactory.
	 *
	 * @param flog Flog to use.
	 */
	public FlogFactory(PluginStore flog) {
		this.flog = flog;

		this.primaryNavigationLinks = new String[]{
					"./index.html", "Index",
					"./Archives-p1.html", "Archives",
					"./AtomFeed.xml", "Atom feed",
					"/?newbookmark=USK@" + WoTOwnIdentities.getRequestURI(this.flog.strings.get("Author")).split("@")[1].split("/")[0] + "/" + this.flog.strings.get("SSKPath") + "/-1/&amp;desc=" + this.flog.strings.get("Title"), "Bookmark this flog"
				};
	}

	/**
	 * Get the parsed list of primary navigation links.
	 *
	 * @param currentUri Actual relative URI (eg. like /Content-...)
	 * @return Parsed xHTML code, only <li>s and </li>s (eg. no <ul>)
	 */
	public String getPrimaryNavigationLinks(String currentUri) {
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < this.primaryNavigationLinks.length; i += 2) {
			boolean thereIsALink = this.primaryNavigationLinks[i] != null;
			// We don't want to cause a NPE, yet we don't want a link if we already are on the page.
			if(thereIsALink) thereIsALink = ! (currentUri != null && this.primaryNavigationLinks[i].endsWith(currentUri));

			sb.append("<li>");
			if(thereIsALink) {
				sb.append("<a href=\"").append(this.primaryNavigationLinks[i]).append("\">");
			}
			sb.append(this.primaryNavigationLinks[i+1]);
			if(thereIsALink) {
				sb.append("</a>");
			}
			sb.append("</li>");

			sb.append("\n\t\t\t\t\t");
		}

		return sb.toString();
	}

	/**
	 * Get a parsed tag list. Works the same as getPrimaryNavigationLinks.
	 * @param currentUri Actual relative URI
	 * @return Parsed xHTML code.
	 */
	public String getTagList(String currentUri) {
		TreeMap<String, Long> tags = new TreeMap<String, Long>();
		for(PluginStore content : this.flog.subStores.values()) {
			if(content.strings.get("ID") == null) continue;
			if(content.strings.get("ID").length() != 7) continue;
			for(String tag : content.stringsArrays.get("Tags")) {
				tags.put(tag, tags.containsKey(tag) ? tags.get(tag) + 1L : 1L);
			}
		}

		StringBuilder sb = new StringBuilder();
		for(String tag : tags.keySet()) {
			if(tag.trim().equals("")) continue;
			boolean thereIsALink = ! (currentUri != null && currentUri.equals("/Tag-" + tag + "-p1.html"));

			sb.append("<li>");
			if(thereIsALink) {
				sb.append("<a href=\"./Tag-" + tag + "-p1.html\">");
			}
			sb.append(DataFormatter.htmlSpecialChars(tag));
			if(thereIsALink) {
				sb.append("</a>");
			}
			sb.append(" (").append(tags.get(tag)).append(")");
			sb.append("</li>");
			sb.append("\n\t\t\t\t\t");
		}

		return sb.toString();
	}

	/**
	 * Get a parsed content block, containing a header and a content div.
	 *
	 * @param content Content to parse.
	 * @return xHTML code.
	 */
	private String getParsedContentBlock(PluginStore content) {
		final StringBuilder mainContent = new StringBuilder();
		String syntax = content.strings.get("ContentSyntax");
		if (syntax == null) {
			syntax = "RawXHTML";
		}

		mainContent.append("<div class=\"content_container\" id=\"c" + content.strings.get("ID") + "\">");
		mainContent.append("<div class=\"content_header\">");
		mainContent.append("<h1>").append(DataFormatter.htmlSpecialChars(content.strings.get("Title"))).append("</h1><p>");
		mainContent.append("<a href=\"./Content-").append(content.strings.get("ID")).append(".html\">Permanent link</a> | <a href=\"./Content-").append(content.strings.get("ID")).append(".html#comments\">Comments</a> | Tags : ");
		boolean first = true;
		for (String tag : content.stringsArrays.get("Tags")) {
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
			mainContent.append("<em>none</em>");
		}

		if (this.shouldPublishDates()) {
			mainContent.append("<br />Creation date : ").append(
					new SimpleDateFormat("yyyy-MM-dd HH:ss").format(new Date(content.longs.get("CreationDate"))));
		}
		mainContent.append("</p></div><div class=\"content_content\">").
				append(ContentSyntax.parseSomeString(content.strings.get("Content"), syntax)).append("</div></div>");

		return mainContent.toString();
	}

	/**
	 * Get the parsed description of the flog.
	 *
	 * @param flog Flog to use.
	 * @return xHTML code of the description, or an empty string if the flog doesn't have a desription.
	 */
	private String getDescription() {
		String syntax = flog.strings.get("SmallDescriptionContentSyntax");
		if (syntax == null) {
			syntax = "RawXHTML";
		}

		String rawDescr = flog.strings.get("SmallDescription");
		if(rawDescr.trim().equals("")) return "";

		return ContentSyntax.parseSomeString(rawDescr, syntax);
	}

	/**
	 * Parse all values that won't change from one page to another.
	 *
	 * @param template Template to parse.
	 * @param uri Actual relative uri.
	 * @return Partially parsed template.
	 */
	private String parseInvariantData(String template, String uri) {
		String authorName = null;
		try {
			authorName = WoTOwnIdentities.getWoTIdentities().get(this.flog.strings.get("Author"));
		} catch (PluginNotFoundException ex) {
			// Shouldn't happen
		}

		template = template.replace("{AuthorName}", authorName);
		template = template.replace("{AuthorNameWithLineBreaks}", DataFormatter.insertIntoString(authorName, "<br />", 18));
		template = template.replace("{FlogName}", DataFormatter.htmlSpecialChars(this.flog.strings.get("Title")));
		template = template.replace("{StyleURI}", "./GlobalStyle.css");
		template = template.replace("{AtomFeedURI}", "./AtomFeed.xml");
		template = template.replace("{AdditionnalMenuContent}", ""); // TODO that might have a use later
		template = template.replace("{FooterContent}", "<!-- Generated by FlogHelper r" + FlogHelper.REVISION + " -->");
		template = template.replace("{PrimaryNavigationLinks}", getPrimaryNavigationLinks(uri));
		template = template.replace("{TagsLinks}", this.getTagList(uri));
		template = template.replace("{ActivelinkIfAny}", this.flog.bytesArrays.containsKey("Activelink") && this.flog.bytesArrays.get("Activelink").length > 0 ? "<p style=\"text-align: center;\"><img src=\"activelink.png\" alt=\"Activelink\" /></p>" : "");

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

		data = BucketTools.makeImmutableBucket(factory, this.getIndex().getBytes());
		name = "index.html";
		fileMap.put(name, new ManifestElement(name, data, DefaultMIMETypes.guessMIMEType(name, true), data.size()));

		data = BucketTools.makeImmutableBucket(factory, this.getAtomFeed().getBytes());
		name = "AtomFeed.xml";
		fileMap.put(name, new ManifestElement(name, data, "application/atom+xml", data.size()));

		data = BucketTools.makeImmutableBucket(factory, this.getCSS().getBytes());
		name = "GlobalStyle.css";
		fileMap.put(name, new ManifestElement(name, data, DefaultMIMETypes.guessMIMEType(name, true), data.size()));

		TreeMap<Long, PluginStore> contents = this.getContentsTreeMap();
		HashMap<String, Long> tagCount = new HashMap<String, Long>();

		for(PluginStore s : contents.values()) {
			final String cID = s.strings.get("ID");
			data = BucketTools.makeImmutableBucket(factory, this.getContentPage(cID).getBytes());
			name = "Content-" + cID + ".html";
			fileMap.put(name, new ManifestElement(name, data, DefaultMIMETypes.guessMIMEType(name, true), data.size()));

			String[] tags = s.stringsArrays.get("Tags");
			if(tags != null && tags.length > 0) {
				for(String tag : tags) {
					tagCount.put(tag, tagCount.containsKey(tag) ? tagCount.get(tag) + 1 : 1);
				}
			}
		}

		final long contentsPerArchivesPage = flog.longs.containsKey("NumberOfContentsOnArchives")
				? flog.longs.get("NumberOfContentsOnArchives") : FlogFactory.DEFAULT_CONTENTS_ON_ARCHIVES;

		for(String tag : tagCount.keySet()) {
			final long pageMax = (long)Math.ceil((double)tagCount.get(tag) / (double)contentsPerArchivesPage);
			for(long i = 1; i <= pageMax; ++i) {
				data = BucketTools.makeImmutableBucket(factory, this.getTagsPage(tag, i).getBytes());
				name = "Tag-" + tag + "-p" + Long.toString(i) + ".html";
				fileMap.put(name, new ManifestElement(name, data, DefaultMIMETypes.guessMIMEType(name, true), data.size()));
			}
		}

		final long pageMax = (long)Math.ceil((double)contents.size() / (double)contentsPerArchivesPage);
		for(long i = 1; i <= pageMax; ++i) {
				data = BucketTools.makeImmutableBucket(factory, this.getArchives(i).getBytes());
				name = "Archives-p" + Long.toString(i) + ".html";
				fileMap.put(name, new ManifestElement(name, data, DefaultMIMETypes.guessMIMEType(name, true), data.size()));
		}

		// TODO generate an index here !

		if(flog.bytesArrays.containsKey("Activelink") && flog.bytesArrays.get("Activelink").length > 0) {
			data = BucketTools.makeImmutableBucket(factory, flog.bytesArrays.get("Activelink"));
			name = "activelink.png";
			fileMap.put(name, new ManifestElement(name, data, DefaultMIMETypes.guessMIMEType(name, true), data.size()));
		}

		if(flog.subStores.get("Attachements") != null) {
			final PluginStore attachements = flog.subStores.get("Attachements");
			for(PluginStore attachement : attachements.subStores.values()) {
				data = BucketTools.makeImmutableBucket(factory, attachement.bytesArrays.get("Content"));
				name = attachement.strings.get("Filename");
				fileMap.put(name, new ManifestElement(name, data, DefaultMIMETypes.guessMIMEType(name, true), data.size()));
			}
		}

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
		final FreenetURI uri = new FreenetURI("USK@" + WoTOwnIdentities.getWoTIdentities("InsertURI").get(this.flog.strings.get("Author")).split("@")[1].split("/")[0] + "/" + flog.strings.get("SSKPath") + "/0");

		Logger.error(this, uri.toString());

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
					 * FCPServer server,
					 * ObjectContainer container
					 */
					ClientPutDir cpd = new ClientPutDir(client, uri, "FlogHelper-" + flog.strings.get("ID"), Integer.MAX_VALUE, RequestStarter.MAXIMUM_PRIORITY_CLASS, ClientRequest.PERSIST_FOREVER, null, false, false, -1, parsedFlog, "index.html", true, false, false, fcp, arg0);
					try {
						fcp.startBlocking(cpd, arg0, arg1);
					} catch (DatabaseDisabledException ex) {
						// Ignore
					}
				} catch (IdentifierCollisionException ex) {
					Logger.error(this, "",  ex);
				} catch (MalformedURLException ex) {
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
		// TODO FreeTalk comments here.
		PluginStore content = this.flog.subStores.get(contentID);

		String genPage = this.parseInvariantData(getTemplate(), null);
		genPage = genPage.replace("{PageTitle}", DataFormatter.htmlSpecialChars(content.strings.get("Title")));
		
		return genPage.replace("{MainContent}", "<div id=\"singlecontent\">" + this.getParsedContentBlock(content) + "</div>");
	}

	/**
	 * Get a parsed /index.html page.
	 *
	 * @return Parsed xHTML page.
	 */
	public String getIndex() {
		String genPage = this.parseInvariantData(getTemplate(), "/index.html");
		genPage = genPage.replace("{PageTitle}", "Index");

		final TreeMap<Long, PluginStore> contents = this.getContentsTreeMap();
		final Long numberOfContentsToShow = this.flog.longs.get("NumberOfContentsOnIndex");

		StringBuilder mainContent = new StringBuilder();
		for (int i = 0; i < numberOfContentsToShow; ++i) {
			if(contents.isEmpty()) break;

			final PluginStore content = contents.lastEntry().getValue();
			contents.remove(content.longs.get("CreationDate"));

			mainContent.append(this.getParsedContentBlock(content));
		}

		return genPage.replace("{MainContent}", this.getDescription() + mainContent.toString());
	}

	/**
	 * Get a parsed /Archives-pPAGE.html
	 *
	 * @param page Page number (from 1 to ceil(numberOfContents/contentsPerPage))
	 * @return Parsed xHTML page
	 */
	public String getArchives(long page) {
		String genPage = this.parseInvariantData(getTemplate(), "/Archives-p" + Long.toString(page) + ".html");
		genPage = genPage.replace("{PageTitle}", "Archives (page " + Long.toString(page) +")");

		final TreeMap<Long, PluginStore> contents = this.getContentsTreeMap();
		final Long numberOfContentsToShow = this.flog.longs.get("NumberOfContentsOnArchives");
		final long numberOfContents = contents.size();

		for(int i = 0; i < (page-1)*numberOfContentsToShow; ++i) {
			final PluginStore content = contents.lastEntry().getValue();
			contents.remove(content.longs.get("CreationDate"));
		}

		StringBuilder mainContent = new StringBuilder();
		for (int i = 0; i < numberOfContentsToShow; ++i) {
			if(contents.isEmpty()) break;

			final PluginStore content = contents.lastEntry().getValue();
			contents.remove(content.longs.get("CreationDate"));

			mainContent.append(this.getParsedContentBlock(content));
		}

		final String pages = "<p class=\"pagination\">Page : " + this.makePagination(numberOfContents, numberOfContentsToShow, page, "./Archives-p{Page}.html") + "</p>";

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
		genPage = genPage.replace("{PageTitle}", "Archives having the tag \"" + DataFormatter.htmlSpecialChars(tag) + "\" (page " + Long.toString(page) +")");

		final TreeMap<Long, PluginStore> contents = this.getContentsTreeMapFilteredByTag(tag);
		final Long numberOfContentsToShow = this.flog.longs.get("NumberOfContentsOnArchives");
		final long numberOfContents = contents.size();

		for(int i = 0; i < (page-1)*numberOfContentsToShow; ++i) {
			final PluginStore content = contents.lastEntry().getValue();
			contents.remove(content.longs.get("CreationDate"));
		}

		StringBuilder mainContent = new StringBuilder();
		for (int i = 0; i < numberOfContentsToShow; ++i) {
			if(contents.isEmpty()) break;

			final PluginStore content = contents.lastEntry().getValue();
			contents.remove(content.longs.get("CreationDate"));

			mainContent.append(this.getParsedContentBlock(content));
		}

		final String pages = "<p class=\"pagination\">Page : " + this.makePagination(numberOfContents, numberOfContentsToShow, page, "./Tag-" + tag + "-p{Page}.html") + "</p>";

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
		feed.append(this.flog.strings.get("ID")).append("-").append(DataFormatter.htmlSpecialChars(this.flog.strings.get("Author")));
		feed.append("</id>\n");

		feed.append("	<title type=\"html\">");
		feed.append(DataFormatter.htmlSpecialChars(this.flog.strings.get("Title")));
		feed.append("</title>\n");

		TreeMap<Long, PluginStore> contents = this.getContentsTreeMap();
		Date mostRecentlyCreationDate = new Date(contents.lastKey());
		if(!this.shouldPublishDates()) {
			mostRecentlyCreationDate = DataFormatter.obfuscateDate(mostRecentlyCreationDate);
		}

		feed.append("	<updated>");
		feed.append(DataFormatter.RFC3339.format(mostRecentlyCreationDate));
		feed.append("</updated>\n");

		String author = FlogHelper.getBaseL10n().getString("BadAuthorDeletedIdentity");
		try {
			if (WoTOwnIdentities.getWoTIdentities().containsKey(flog.strings.get("Author"))) {
				author = WoTOwnIdentities.getWoTIdentities().get(flog.strings.get("Author"));
			}
		} catch (PluginNotFoundException ex) {
			// Safe to ignore.
		}

		feed.append("	<author><name>");
		feed.append(DataFormatter.htmlSpecialChars(author));
		feed.append("</name></author>\n");

		feed.append("	<link rel=\"self\" href=\"./AtomFeed.xml\" />\n");
		feed.append("	<generator version=\"r" + FlogHelper.REVISION + "\">FlogHelper</generator>\n");

		feed.append("	<subtitle type=\"xhtml\">\n" +
				"		<div xmlns=\"http://www.w3.org/1999/xhtml\">\n");
		feed.append(this.getDescription());
		feed.append("\n		</div>\n" +
				"	</subtitle>\n");

		for(Long creationDate : contents.descendingKeySet()) {
			PluginStore content = contents.get(creationDate);
			feed.append("	<entry>\n");
			feed.append("		<id>tag:freenet-");
			feed.append(this.flog.strings.get("ID")).append("-").append(content.strings.get("ID")).append("-").append(DataFormatter.htmlSpecialChars(this.flog.strings.get("Author")));
			feed.append("</id>\n");

			feed.append("		<title type=\"text\">");
			feed.append(content.strings.get("Title"));
			feed.append("</title>\n");

			Date modifDate = new Date(content.longs.get("LastModification"));
			Date creaDate = new Date(creationDate);
			if(!this.shouldPublishDates()) {
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
			feed.append(ContentSyntax.parseSomeString(content.strings.get("Content"),
					content.strings.get("ContentSyntax") == null ? "RawXHTML" : content.strings.get("ContentSyntax")));
			feed.append("\n			</div>\n" +
					"		</content>\n");
			feed.append("		<link rel=\"alternate\" href=\"./Content-" + content.strings.get("ID") + ".html\" />\n");
			feed.append("</entry>\n");
		}


		return feed.append("</feed>").toString();
	}

	/**
	 * Get all the contents, sorted by ascending creation date.
	 *
	 * @return Tree of all the contents.
	 */
	public TreeMap<Long, PluginStore> getContentsTreeMap() {
		TreeMap<Long, PluginStore> map = new TreeMap<Long, PluginStore>();

		for(String id : this.flog.subStores.keySet()) {
			if(id.length() != 7) continue;
			final PluginStore content = this.flog.subStores.get(id);
			map.put(content.longs.get("CreationDate"), content);
		}

		return map;
	}

	/**
	 * Get all the contents tagged with a particular tag, sorted by ascending creation date.
	 *
	 * @param tag Tag to match.
	 * @return Tree of contents tagged with the specified tag.
	 */
	private TreeMap<Long, PluginStore> getContentsTreeMapFilteredByTag(String tag) {
		TreeMap<Long, PluginStore> map = new TreeMap<Long, PluginStore>();

		for (String id : this.flog.subStores.keySet()) {
			final PluginStore content = this.flog.subStores.get(id);
			if (content.stringsArrays.get("Tags") != null) {
				boolean isTagInside = false;
				for (String s : content.stringsArrays.get("Tags")) {
					if (s.equals(tag)) {
						isTagInside = true;
						break;
					}
				}

				if (isTagInside) {
					map.put(content.longs.get("CreationDate"), content);
				}
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

		if(page == pageMin) {
			sb.append("&lt; Previous");
		} else sb.append("<a href=\"").append(pageUri.replace("{Page}", Long.toString(page - 1L))).append("\">&lt; Previous</a>");
		sb.append(" | ");
		if(page == pageMax) {
			sb.append("Next &gt;");
		} else sb.append("<a href=\"").append(pageUri.replace("{Page}", Long.toString(page + 1L))).append("\">Next &gt;</a>");
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

	/**
	 * Should we publish creation/modification dates ?
	 * @return true if we should.
	 */
	public boolean shouldPublishDates() {
		return flog.booleans.get("PublishContentModificationDate") != null &&
				flog.booleans.get("PublishContentModificationDate") == true;
	}
}
