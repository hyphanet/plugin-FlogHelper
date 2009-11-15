/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
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
		
		if(!this.parentFlog.getStore().subStores.get("Attachements").subStores.containsKey(attachmentID)) {
			throw new NullPointerException("Attachment doesn't exist!");
		}
		
		this.attachment = this.parentFlog.getStore().subStores.get("Attachements").subStores.get(attachmentID);
	}

	public PluginStoreAttachment(PluginStoreFlog flog, String name, byte[] data) {
		this.attachment = new PluginStore();
		this.parentFlog = flog;

		this.attachment.strings.put("ID", DataFormatter.createSubStoreUniqueID(this.parentFlog.getStore().subStores.get("Attachements")));
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
