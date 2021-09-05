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

import freenet.pluginmanager.PluginStore;
import java.util.Date;
import plugins.floghelper.data.Attachment;
import plugins.floghelper.data.DataFormatter;
import plugins.floghelper.data.Flog;

/**
 * Attachment stored in a PluginStore.
 *
 * @author Artefact2
 */
public class PluginStoreAttachment extends Attachment {

	private final PluginStore attachment;
	private final PluginStoreFlog parentFlog;

	public PluginStoreAttachment(PluginStoreFlog flog, String attachmentID) {
		this.parentFlog = flog;
		
		if(!this.parentFlog.getStore().subStores.get("Attachments").subStores.containsKey(attachmentID)) {
			throw new NullPointerException("Attachment doesn't exist!");
		}
		
		this.attachment = this.parentFlog.getStore().subStores.get("Attachments").subStores.get(attachmentID);
	}

	public PluginStoreAttachment(PluginStoreFlog flog, String name, byte[] data) {
		this.attachment = new PluginStore();
		this.parentFlog = flog;

		if(!this.parentFlog.getStore().subStores.containsKey("Attachments")) {
			this.parentFlog.getStore().subStores.put("Attachments", new PluginStore());
		}

		this.attachment.strings.put("ID", DataFormatter.createSubStoreUniqueID(this.parentFlog.getStore().subStores.get("Attachments")));
		this.setName(name);
		this.attachment.bytesArrays.put("Content", data);
		this.attachment.longs.put("CreationDate", System.currentTimeMillis());
	}

	public PluginStore getStore() {
		return this.attachment;
	}
	
	public Flog getParent() {
		return parentFlog;
	}

	public String getID() {
		return this.attachment.strings.get("ID");
	}

	public String getName() {
		return this.attachment.strings.get("Filename");
	}

	public void setName(String s) {
		this.attachment.strings.put("Filename", s);
	}

	public Date getInsertionDate() {
		return new Date(attachment.longs.get("CreationDate"));
	}

	public byte[] getData() {
		return attachment.bytesArrays.get("Content");
	}
}
