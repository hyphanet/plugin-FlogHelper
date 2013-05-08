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
package plugins.floghelper.data.pluginstore;

import freenet.keys.FreenetURI;
import freenet.l10n.BaseL10n.LANGUAGE;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginStore;
import freenet.support.Logger;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Vector;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.data.Attachment;
import plugins.floghelper.data.Content;
import plugins.floghelper.data.DataFormatter;
import plugins.floghelper.data.Flog;
import plugins.floghelper.fcp.wot.WoTOwnIdentities;
import plugins.floghelper.ui.flog.FlogFactory;

/**
 * Flog stored in a PluginStore.
 *
 * @author Artefact2
 */
public class PluginStoreFlog extends Flog {
	private final PluginStore flog;

	public PluginStoreFlog(String flogID) {
		PluginStore flogs = FlogHelper.getStore();

		if(!flogs.subStores.containsKey(flogID)) {
			throw new NullPointerException("FlogID maps to nothing !");
		}
		
		this.flog = flogs.subStores.get(flogID);
	}

	public PluginStoreFlog() {
		this.flog = new PluginStore();
		this.flog.strings.put("ID", DataFormatter.createSubStoreUniqueID(FlogHelper.getStore()));

		this.setShortDescription("");
		this.setTitle("New untitled flog");
		this.setTheme(FlogFactory.THEMES[0]);
		this.setAuthorID("");
	}

	public static Vector<PluginStoreFlog> getFlogs() {
		Vector<PluginStoreFlog> flogs = new Vector<PluginStoreFlog>();

		for(String flogID : FlogHelper.getStore().subStores.keySet()) {
			flogs.add(new PluginStoreFlog(flogID));
		}

		return flogs;
	}

	public PluginStore getStore() {
		return this.flog;
	}

	public String getID() {
		return this.flog.strings.get("ID");
	}

	public String getTitle() {
		return this.flog.strings.get("Title");
	}

	public void setTitle(String s) {
		this.flog.strings.put("Title", s);
	}

	public String getAuthorID() {
		return this.flog.strings.get("Author");
	}

	public void setAuthorID(String s) {
		this.flog.strings.put("Author", s);
	}

	public String getAuthorName() {
		final String author;
		Map<String, String> idents = null;

		try {
			idents = WoTOwnIdentities.getWoTIdentities();
		} catch (PluginNotFoundException ex) {
			Logger.error(this, "Couldn't talk to WoT.", ex);
			return "**WoTUnreachable**";
		}

		if (idents.containsKey(this.getAuthorID())) {
			author = idents.get(this.getAuthorID());
		} else {
			author = FlogHelper.getBaseL10n().getString("BadAuthorDeletedIdentity");
		}

		return author;
	}

	public long getNumberOfContents() {
		return this.getContents().size();
	}

	public Vector<Content> getContents() {
		Vector<Content> contents = new Vector<Content>();

		for(PluginStore s : this.flog.subStores.values()) {
			if(s.strings.containsKey("ID") && s.strings.get("ID").length() == 7) {
				contents.add(new PluginStoreContent(this, s.strings.get("ID")));
			}
		}
		
		return contents;
	}

	public Content getContentByID(String s) {
		return new PluginStoreContent(this, s);
	}

	public void putContent(Content c) {
		if(!(c instanceof PluginStoreContent)) {
			throw new UnsupportedOperationException();
		}

		PluginStoreContent psC = (PluginStoreContent) c;
		this.flog.subStores.put(psC.getID(), psC.getStore());
	}

	public void deleteContent(String contentID) {
		this.flog.subStores.remove(contentID);
	}

	public String getShortDescription() {
		return this.flog.strings.get("SmallDescription");
	}

	public void setShortDescription(String s) {
		this.flog.strings.put("SmallDescription", s);
	}

	public boolean shouldPublishDates() {
		if(!this.flog.booleans.containsKey("PublishContentModificationDate")) {
			return Flog.DEFAULT_SHOULD_PUBLISH_DATES;
		}
		return this.flog.booleans.get("PublishContentModificationDate");
	}

	public void shouldPublishDates(boolean b) {
		this.flog.booleans.put("PublishContentModificationDate", b);
	}

	public boolean shouldPublishLibraryIndex() {
		if(!this.flog.booleans.containsKey("InsertLibraryIndex")) {
			return Flog.DEFAULT_SHOULD_INSERT_INDEX;
		}
		return this.flog.booleans.get("InsertLibraryIndex");
	}

	public void shouldPublishLibraryIndex(boolean b) {
		this.flog.booleans.put("InsertLibraryIndex", b);
	}

	public boolean shouldSortTagsByCount()
		{if(!this.flog.booleans.containsKey("SortTagsByCount")) {
			return Flog.DEFAULT_SHOULD_SORT_TAGS_BY_COUNT;
		}
		return this.flog.booleans.get("SortTagsByCount");
	}

	public void shouldSortTagsByCount(boolean b) {
		this.flog.booleans.put("SortTagsByCount", b);
	}

	public boolean overrideTemplate() {
		if(!this.flog.booleans.containsKey("OverrideTemplate")) {
			return false;
		}
		return this.flog.booleans.get("OverrideTemplate");
	}

	public void overrideTemplate(boolean b) {
		this.flog.booleans.put("OverrideTemplate", b);
	}

	public boolean overrideCSS() {
		if(!this.flog.booleans.containsKey("OverrideCSS")) {
			return false;
		}
		return this.flog.booleans.get("OverrideCSS");
	}

	public void overrideCSS(boolean b) {
		this.flog.booleans.put("OverrideCSS", b);
	}

	public long getNumberOfContentsOnIndex() {
		if(!this.flog.longs.containsKey("NumberOfContentsOnIndex")) {
			return Flog.DEFAULT_CONTENTS_ON_INDEX;
		}
		return this.flog.longs.get("NumberOfContentsOnIndex");
	}

	public void setNumberOfContentsOnIndex(long l) {
		this.flog.longs.put("NumberOfContentsOnIndex", l);
	}

	public long getNumberOfContentsOnArchives() {
		if(!this.flog.longs.containsKey("NumberOfContentsOnArchives")) {
			return Flog.DEFAULT_CONTENTS_ON_ARCHIVES;
		}
		return this.flog.longs.get("NumberOfContentsOnArchives");
	}

	public void setNumberOfContentsOnArchives(long l) {
		this.flog.longs.put("NumberOfContentsOnArchives", l);
	}

	public String getCSSOverride() {
		return this.flog.strings.get("OverrideCSSValue");
	}

	public void setCSSOverride(String s) {
		this.flog.strings.put("OverrideCSSValue", s);
	}

	public String getTemplateOverride() {
		return this.flog.strings.get("OverrideTemplateValue");
	}

	public void setTemplateOverride(String s) {
		this.flog.strings.put("OverrideTemplateValue", s);
	}

	public String getTheme() {
		return this.flog.strings.get("Theme");
	}

	public void setTheme(String s) {
		this.flog.strings.put("Theme", s);
	}

	public byte[] getActivelink() {
		return this.flog.bytesArrays.get("Activelink");
	}

	public void setActivelink(byte[] b) {
		this.flog.bytesArrays.put("Activelink", b);
	}

	public String getSSKPath() {
		if(!this.flog.strings.containsKey("SSKPath")) {
			return Flog.DEFAULT_SSK_PATH;
		}
		return this.flog.strings.get("SSKPath");
	}

	public void setSSKPath(String s) {
		this.flog.strings.put("SSKPath", s);
	}

	public FreenetURI getRequestURI() throws MalformedURLException, PluginNotFoundException {
		return new FreenetURI("USK@" + WoTOwnIdentities.getWoTIdentities("RequestURI").get(this.getAuthorID()).split("@")[1].split("/")[0] + "/" + this.getSSKPath() + "/" + Long.toString(this.getLatestUSKEdition()) + "/");
	}

	public FreenetURI getInsertURI() throws PluginNotFoundException, MalformedURLException {
		return new FreenetURI("USK@" + WoTOwnIdentities.getWoTIdentities("InsertURI").get(this.getAuthorID()).split("@")[1].split("/")[0] + "/" + this.getSSKPath() + "/0");
	}

	public long getNumberOfAttachments() {
		return this.flog.subStores.get("Attachments").subStores.size();
	}

	public Vector<Attachment> getAttachments() {
		Vector<Attachment> attachments = new Vector<Attachment>();

		if(this.flog.subStores.containsKey("Attachments")) {
			for(PluginStore s : this.flog.subStores.get("Attachments").subStores.values()) {
				attachments.add(new PluginStoreAttachment(this, s.strings.get("Filename")));
			}
		}

		return attachments;
	}

	public Attachment getAttachmentByName(String s) {
		return new PluginStoreAttachment(this, s);
	}

	public void putAttachment(Attachment c) {
		if(!(c instanceof PluginStoreAttachment)) {
			throw new UnsupportedOperationException();
		}

		PluginStoreAttachment psA = (PluginStoreAttachment) c;
		this.flog.subStores.get("Attachments").subStores.put(psA.getName(), psA.getStore());
	}

	public void deleteAttachment(String attachmentID) {
		this.flog.subStores.get("Attachments").subStores.remove(attachmentID);
	}

	public boolean hasActivelink() {
		return this.flog.bytesArrays.containsKey("Activelink") && this.getActivelink().length > 0;
	}

	public void destroyActivelink() {
		this.flog.bytesArrays.remove("Activelink");
	}

	/** All this crap is to do with an unfortunate problem with flog.db4o, and can safely be ignored for 
	 * flogs created since then. */
	
	public boolean shouldPublishStoreDump() {
		if(!this.flog.booleans.containsKey("InsertPluginStoreDump")) {
			return Flog.DEFAULT_SHOULD_INSERT_STOREDUMP;
		}
		return this.flog.booleans.get("InsertPluginStoreDump");
	}

	public void shouldPublishStoreDump(boolean b) {
		this.flog.booleans.put("InsertPluginStoreDump", b);
	}

	public boolean userWarnedTainted() {
		if(!this.flog.booleans.containsKey("UserWarnedTainted")) {
			return false;
		}
		return this.flog.booleans.get("UserWarnedTainted");
	}

	public void userWarnedTainted(boolean b) {
		this.flog.booleans.put("UserWarnedTainted", b);
	}
	
	public String taintedHostname() {
		if(!this.flog.strings.containsKey("TaintedHostname")) {
			String host;
			InetAddress addr;
			try {
				addr = java.net.InetAddress.getLocalHost();
				host = addr.getHostName();
			} catch (UnknownHostException e) {
				try {
					addr = InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 });
					host = addr.getHostName();
				} catch (UnknownHostException e1) {
					host = "(unknown)";
					System.err.println("Unable to get localhost hostname?!");
				}
			}
			flog.strings.put("TaintedHostname", host);
			return host;
		} else
			return this.flog.strings.get("TaintedHostname");
	}

	public void putFlog() {
		FlogHelper.getStore().subStores.put(this.getID(), this.getStore());
	}

	public Content newContent() {
		return new PluginStoreContent(this);
	}

	public Attachment newAttachment(String attachmentName, byte[] data) {
		return new PluginStoreAttachment(this, attachmentName, data);
	}

	public long getLatestUSKEdition() {
		if(!this.flog.longs.containsKey("USKLatestEdition")) {
			this.flog.longs.put("USKLatestEdition", 0L);
		}

		return this.flog.longs.get("USKLatestEdition");
	}

	public byte[] exportFlog() {
		return this.getStore().exportStore();
	}

	public static boolean hasFlog(String flogID) {
		return FlogHelper.getStore().subStores.containsKey(flogID);
	}

	public boolean hasContent(String contentID) {
		return this.getStore().subStores.containsKey(contentID);
	}

	public void setLatestUSKEdition(long edition) {
		this.flog.longs.put("USKLatestEdition", edition);
		FlogHelper.putStore();
	}

	public LANGUAGE getLang() {
		String shortCode = null;
		if(this.getStore().strings.containsKey("Lang")) {
			shortCode = this.getStore().strings.get("Lang");
		} else {
			shortCode = "en";
		}

		return LANGUAGE.mapToLanguage(shortCode);
	}

	protected void setLangInner(LANGUAGE l) {
		this.getStore().strings.put("Lang", l.shortCode);
	}
}
