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
package plugins.floghelper.data;

import com.db4o.ObjectContainer;
import freenet.client.async.ClientContext;
import freenet.client.async.USKCallback;
import freenet.keys.FreenetURI;
import freenet.keys.USK;
import freenet.l10n.BaseL10n;
import freenet.l10n.BaseL10n.LANGUAGE;
import freenet.l10n.PluginL10n;
import freenet.node.RequestStarter;
import freenet.pluginmanager.FredPluginBaseL10n;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.support.Logger;
import java.util.Vector;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.fcp.wot.WoTContexts;

/**
 * Abstract flog representation.
 *
 * @author Artefact2
 */
public abstract class Flog {

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
	 * Should we insert a Library index by default ?
	 */
	public static final boolean DEFAULT_SHOULD_INSERT_INDEX = true;

	/**
	 * Should we sort tags alphabetically or by count ? (ie, show more important
	 * tags first)
	 */
	public static final boolean DEFAULT_SHOULD_SORT_TAGS_BY_COUNT = false;

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

	/**
	 * Filename of the inserted Flog backup.
	 */
	public static final String STORE_DUMP_NAME = "flog.db4o";

	private USKCallback uskCallback = null;
	private BaseL10n myBaseL10n = null;

	abstract public String getID();
	abstract public String getTitle();
	abstract public void   setTitle(String s);
	abstract public String getAuthorID();
	abstract public void   setAuthorID(String s);
	abstract public String getAuthorName();

	abstract public LANGUAGE getLang();
	abstract protected void setLangInner(LANGUAGE l);
	public void setLang(LANGUAGE l) {
		this.setLangInner(l);
		this.getBaseL10n().setLanguage(l);
	}
	public BaseL10n getBaseL10n() {
		if (this.myBaseL10n == null) {
			this.myBaseL10n = new PluginL10n(new FredPluginBaseL10n() {

				public void setLanguage(LANGUAGE arg0) {
					// Doesn't make any sense here, because
					// it would override the user setting, who might
					// write a flog in another language than the node's.
				}

				public String getL10nFilesBasePath() {
					return FlogHelper.l10nFilesBasePath;
				}

				public String getL10nFilesMask() {
					return FlogHelper.l10nFilesMask;
				}

				public String getL10nOverrideFilesMask() {
					return FlogHelper.l10nOverrideFilesMask;
				}

				public ClassLoader getPluginClassLoader() {
					return FlogHelper.class.getClassLoader();
				}
			}, this.getLang()).getBase();
		}

		return this.myBaseL10n;
	}

	abstract public long getNumberOfContents();
	abstract public Vector<Content> getContents();
	abstract public Content getContentByID(String s);
	abstract public void putContent(Content c);
	abstract public void deleteContent(String contentID);
	abstract public Content newContent();
	abstract public boolean hasContent(String contentID);

	abstract public long getNumberOfAttachments();
	abstract public Vector<Attachment> getAttachments();
	abstract public Attachment getAttachmentByName(String s);
	abstract public void putAttachment(Attachment c);
	abstract public void deleteAttachment(String attachmentName);
	abstract public Attachment newAttachment(String attachmentName, byte[] data);

	abstract public String getShortDescription();
	abstract public void   setShortDescription(String s);

	abstract public boolean shouldPublishDates();
	abstract public void    shouldPublishDates(boolean b);
	/** DISABLED, but kept for now to warn the user. THIS IS GROSSLY INSECURE!!! */
	abstract public boolean shouldPublishStoreDump();
	abstract public void    shouldPublishStoreDump(boolean b);
	abstract public boolean shouldPublishLibraryIndex();
	abstract public void    shouldPublishLibraryIndex(boolean b);
	abstract public boolean shouldSortTagsByCount();
	abstract public void    shouldSortTagsByCount(boolean b);
	abstract public boolean overrideTemplate();
	abstract public void    overrideTemplate(boolean b);
	abstract public boolean overrideCSS();
	abstract public void    overrideCSS(boolean b);
	
	abstract public long getNumberOfContentsOnIndex();
	abstract public void setNumberOfContentsOnIndex(long l);
	abstract public long getNumberOfContentsOnArchives();
	abstract public void setNumberOfContentsOnArchives(long l);

	abstract public String getCSSOverride();
	abstract public void   setCSSOverride(String s);
	abstract public String getTemplateOverride();
	abstract public void   setTemplateOverride(String s);
	abstract public String getTheme();
	abstract public void   setTheme(String s);

	abstract public boolean hasActivelink();
	abstract public byte[] getActivelink();
	abstract public void   setActivelink(byte[] b);
	abstract public void destroyActivelink();

	abstract public String getSSKPath();
	abstract public void   setSSKPath(String s);

	abstract public FreenetURI getRequestURI() throws Exception;
	abstract public FreenetURI getInsertURI() throws Exception;
	abstract public long getLatestUSKEdition();
	abstract public void setLatestUSKEdition(long edition);

	/**
	 * Get the numeric id of this flog. The flog properties <strong>for
	 * this author only</strong> will begin with Flog.X., where X is this
	 * numeric ID. It can change when the author changes.
	 */
	public long getNumericPropertyID() throws PluginNotFoundException {
		long i = 0;
		String id;
		while((id = WoTContexts.getProperty(getAuthorID(), "Flog." + Long.toString(i) + ".ID")) != null && !id.equals(getID())) {
			++i;
		}

		return i;
	}

	public USKCallback getUSKCallback() {
		if(this.uskCallback == null) {
			uskCallback = new USKCallback() {
				public void onFoundEdition(long arg0, USK arg1, ObjectContainer arg2, ClientContext arg3, boolean arg4, short arg5, byte[] arg6, boolean arg7, boolean arg8) {
					Logger.debug(this, "Found edition " + Long.toString(arg0));
					if(arg0 > getLatestUSKEdition()) {
						setLatestUSKEdition(arg0);
						Logger.debug(this, "Updating contexes…");
						updateContextes();
					}
				}

				public short getPollingPriorityNormal() {
					return RequestStarter.UPDATE_PRIORITY_CLASS;
				}

				public short getPollingPriorityProgress() {
					return RequestStarter.UPDATE_PRIORITY_CLASS;
				}
			};
		}
		return this.uskCallback;
	}

	private void updateContextes() {
		try {
			final long numericID = getNumericPropertyID();
			WoTContexts.addProperty(getAuthorID(), "Flog." + numericID + ".ID", getID());
			WoTContexts.addProperty(getAuthorID(), "Flog." + numericID + ".Path", getSSKPath());
			WoTContexts.addProperty(getAuthorID(), "Flog." + numericID + ".LatestEdition", Long.toString(getLatestUSKEdition()));
			WoTContexts.addProperty(getAuthorID(), "Flog.MostRecent", Long.toString(numericID));

			// Try to remove old properties, created by older revisions of
			// FlogHelper to keep the properties list clean.
			WoTContexts.removeProperty(getAuthorID(), "Flog." + getID() + ".Path");
			WoTContexts.removeProperty(getAuthorID(), "Flog." + getID() + ".LatestEdition");
		} catch (PluginNotFoundException ex) {
			// Too bad.
			// We'll update it next time then.
		}
	}

	abstract public void putFlog();

	abstract public byte[] exportFlog();
}
