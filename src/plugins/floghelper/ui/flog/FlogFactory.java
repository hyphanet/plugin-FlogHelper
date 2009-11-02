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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.contentsyntax.ContentSyntax;

/**
 * TODO: RSS2 & Atom feeds
 *
 * @author Artefact2
 */
public class FlogFactory {
	public static String[] primaryNavigationLinks = new String[] {
		"/", "Index",
		"/Archives-p1.html", "Archives",
		"/AtomFeed.xml", "Atom feed",
		"/RSSFeed.xml", "RSS feed",
		null, "<form method=\"post\" action=\"\"><p>Search : <input type=\"text\" size=\"10\" /><input type=\"submit\" /></p></form>"
	};

	private final PluginStore flog;

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

	public String getTemplate() {
		if(flog.booleans.get("OverrideTemplate") != null && flog.booleans.get("OverrideTemplate") == true) {
			return flog.strings.get("OverrideTemplateValue");
		} else return getResourceAsString("plugins/floghelper/ui/flog/GlobalTemplate.html");
	}

	public String getCSS() {
		if(flog.booleans.get("OverrideCSS") != null && flog.booleans.get("OverrideCSS") == true) {
			return flog.strings.get("OverrideCSSValue");
		} else return getResourceAsString("plugins/floghelper/ui/flog/GlobalStyle.css");

	}

	public FlogFactory(PluginStore flog) {
		this.flog = flog;
	}

	public String getPrimaryNavigationLinks(String currentUri) {
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < FlogFactory.primaryNavigationLinks.length; i += 2) {
			boolean thereIsALink = FlogFactory.primaryNavigationLinks[i] != null;
			// We don't want to cause a NPE, yet we don't want a link if we already are on the page.
			if(thereIsALink) thereIsALink = ! (currentUri != null && currentUri.equals(FlogFactory.primaryNavigationLinks[i]));

			sb.append("<li>");
			if(thereIsALink) {
				sb.append("<a href=\".").append(FlogFactory.primaryNavigationLinks[i]).append("\">");
			}
			sb.append(FlogFactory.primaryNavigationLinks[i+1]);
			if(thereIsALink) {
				sb.append("</a>");
			}
			sb.append("</li>");

			sb.append("\n\t\t\t\t\t");
		}

		return sb.toString();
	}

	public String getTagList(String currentUri) {
		TreeMap<String, Long> tags = new TreeMap<String, Long>();
		for(PluginStore content : this.flog.subStores.values()) {
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
			sb.append(tag);
			if(thereIsALink) {
				sb.append("</a>");
			}
			sb.append(" (").append(tags.get(tag)).append(")");
			sb.append("</li>");
			sb.append("\n\t\t\t\t\t");
		}

		return sb.toString();
	}

	private String getParsedContentBlock(PluginStore content) {
		final StringBuilder mainContent = new StringBuilder();
		String syntax = content.strings.get("ContentSyntax");
		if (syntax == null) {
			syntax = "RawXHTML";
		}

		try {
			mainContent.append("<div class=\"content_container\">");
			mainContent.append("<div class=\"content_header\">");
			mainContent.append("<h1>").append(content.strings.get("Title")).append("</h1><p>");
			mainContent.append("<a href=\"./Content-").append(content.strings.get("ID")).append(".html\">Permanent link</a> | <a href=\"./Content-").append(content.strings.get("ID")).append(".html#comments\">Comments</a> | Tags : ");
			boolean first = true;
			for (String tag : content.stringsArrays.get("Tags")) {
				if(tag.trim().equals("")) continue;
				if (first) {
					first = false;
				} else {
					mainContent.append(", ");
				}
				mainContent.append("<a href=\"./Tag-").append(tag).append("-p1.html\">").append(tag).append("</a>");
			}

			if (first) {
				mainContent.append("<em>none</em>");
			}

			if (flog.booleans.get("PublishContentModificationDate") != null &&
					flog.booleans.get("PublishContentModificationDate") == true) {
				mainContent.append("<br />Creation date : ").append(
						new SimpleDateFormat("yyyy-MM-dd HH:ss").format(new Date(content.longs.get("CreationDate"))));
			}
			mainContent.append("</p></div><div class=\"content_content\">").
					append(((ContentSyntax) Class.forName("plugins.floghelper.contentsyntax." + syntax).newInstance()).parseSomeString(content.strings.get("Content"))).append("</div></div>");
		} catch (InstantiationException ex) {
			Logger.error(this, "Cannot instanciate Content syntax " + content.strings.get("ContentSyntax"));
		} catch (IllegalAccessException ex) {
			Logger.error(this, "Cannot instanciate Content syntax " + content.strings.get("ContentSyntax"));
		} catch (ClassNotFoundException ex) {
			Logger.error(this, "Cannot instanciate Content syntax " + content.strings.get("ContentSyntax"));
		}

		return mainContent.toString();
	}

	public String parseInvariantData(String template, String uri) {
		template = template.replace("{FlogAuthor}", this.flog.strings.get("Author"));
		template = template.replace("{FlogName}", this.flog.strings.get("Title"));
		template = template.replace("{StyleURI}", "./GlobalStyle.css");
		template = template.replace("{AtomFeedURI}", "./AtomFeed.xml");
		template = template.replace("{AdditionnalMenuContent}", ""); // FIXME that might have a use later
		template = template.replace("{FooterContent}", "<!-- Generated by FlogHelper r" + FlogHelper.REVISION + " -->");
		template = template.replace("{PrimaryNavigationLinks}", this.getPrimaryNavigationLinks(uri));
		template = template.replace("{TagsLinks}", this.getTagList(uri));

		return template;
	}

	public HashMap<String, String> parseAllFlog() {
		HashMap<String, String> fileMap = new HashMap<String, String>();

		return fileMap;
	}

	public String getContentPage(String contentID) {
		// TODO FreeTalk comments here.
		PluginStore content = this.flog.subStores.get(contentID);

		String genPage = this.parseInvariantData(getTemplate(), null);
		genPage = genPage.replace("{PageTitle}", content.strings.get("Title"));
		
		return genPage.replace("{MainContent}", "<div id=\"singlecontent\">" + this.getParsedContentBlock(content) + "</div>");
	}

	public String getIndex() {
		String genPage = this.parseInvariantData(getTemplate(), "/");
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

		return genPage.replace("{MainContent}", mainContent.toString());
	}

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

	public String getTagsPage(String tag, long page) {
		String genPage = this.parseInvariantData(getTemplate(), "/Tag-" + tag + "-p" + Long.toString(page) + ".html");
		genPage = genPage.replace("{PageTitle}", "Archives having the tag \"" + tag + "\" (page " + Long.toString(page) +")");

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

	private TreeMap<Long, PluginStore> getContentsTreeMap() {
		TreeMap<Long, PluginStore> map = new TreeMap<Long, PluginStore>();

		for(String id : this.flog.subStores.keySet()) {
			final PluginStore content = this.flog.subStores.get(id);
			map.put(content.longs.get("CreationDate"), content);
		}

		return map;
	}

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
}
