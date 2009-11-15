/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.data.pluginstore;

import freenet.keys.FreenetURI;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginStore;
import freenet.support.Logger;
import java.net.MalformedURLException;
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
		this.setShortDescriptionSyntax("YAWKL");
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

	public String getShortDescriptionSyntax() {
		return this.flog.strings.get("SmallDescriptionContentSyntax");
	}

	public void setShortDescriptionSyntax(String s) {
		this.flog.strings.put("SmallDescriptionContentSyntax", s);
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
		return new FreenetURI("USK@" + WoTOwnIdentities.getWoTIdentities("RequestURI").get(this.getAuthorID()).split("@")[1].split("/")[0] + "/" + this.getSSKPath() + "/-1/");
	}

	public FreenetURI getInsertURI() throws PluginNotFoundException, MalformedURLException {
		return new FreenetURI("USK@" + WoTOwnIdentities.getWoTIdentities("InsertURI").get(this.getAuthorID()).split("@")[1].split("/")[0] + "/" + this.getSSKPath() + "/0");
	}

	public long getNumberOfAttachments() {
		return this.flog.subStores.get("Attachements").subStores.size();
	}

	public Vector<Attachment> getAttachments() {
		Vector<Attachment> attachments = new Vector<Attachment>();

		for(PluginStore s : this.flog.subStores.get("Attachements").subStores.values()) {
			attachments.add(new PluginStoreAttachment(this, s.strings.get("Filename")));
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
		this.flog.subStores.get("Attachements").subStores.put(psA.getName(), psA.getStore());
	}

	public void deleteAttachment(String attachmentID) {
		this.flog.subStores.get("Attachements").subStores.remove(attachmentID);
	}

	public boolean hasActivelink() {
		return this.flog.bytesArrays.containsKey("Activelink") && this.getActivelink().length > 0;
	}

	public void destroyActivelink() {
		this.flog.bytesArrays.remove("Activelink");
	}

	public boolean shouldPublishStoreDump() {
		if(!this.flog.booleans.containsKey("InsertPluginStoreDump")) {
			return Flog.DEFAULT_SHOULD_INSERT_STOREDUMP;
		}
		return this.flog.booleans.get("InsertPluginStoreDump");
	}

	public void shouldPublishStoreDump(boolean b) {
		this.flog.booleans.put("InsertPluginStoreDump", b);
	}

	public void putFlog() {
		FlogHelper.getStore().subStores.put(this.getID(), this.getStore());
	}

	public Content newContent() {
		return new PluginStoreContent(this);
	}

	public Attachment newAttachment(String attachmentName, byte[] data) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
