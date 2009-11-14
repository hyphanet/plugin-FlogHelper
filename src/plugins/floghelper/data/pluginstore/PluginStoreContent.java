/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.data.pluginstore;

import freenet.pluginmanager.PluginStore;
import java.util.Date;
import java.util.Vector;
import plugins.floghelper.data.Content;
import plugins.floghelper.data.DataFormatter;
import plugins.floghelper.data.Flog;

/**
 * Content stored in a PluginStore.
 *
 * @author Artefact2
 */
public class PluginStoreContent extends Content {

	private final PluginStore content;
	private final PluginStoreFlog parentFlog;

	public PluginStoreContent(PluginStoreFlog parentFlog, String contentID) {
		this.parentFlog = parentFlog;

		if(!this.parentFlog.getStore().subStores.containsKey(contentID)) {
			throw new NullPointerException("Content doesn't exist!");
		}

		this.content = this.parentFlog.getStore().subStores.get(contentID);
	}

	public PluginStoreContent(PluginStoreFlog flog) {
		this.content = new PluginStore();
		this.parentFlog = flog;

		this.content.strings.put("ID", DataFormatter.createSubStoreUniqueID(this.parentFlog.getStore()));
		this.content.longs.put("CreationDate", System.currentTimeMillis());
		this.setContent("");
		this.setContentSyntax("RawXHTML");
		this.setDraft(Content.DEFAULT_DRAFT_STATUS);
		this.setTitle("Unnamed new content");
		this.setTags(new Vector<String>());
	}

	public PluginStore getStore() {
		return this.content;
	}
	
	private void updateModificationTime() {
		this.content.longs.put("LastModification", System.currentTimeMillis());
	}

	public Flog getParent() {
		return this.parentFlog;
	}

	public String getID() {
		return this.content.strings.get("ID");
	}

	public String getTitle() {
		return this.content.strings.get("Title");
	}

	public void setTitle(String s) {
		this.content.strings.put("Title", s);
		this.updateModificationTime();
	}

	public String getContent() {
		return this.content.strings.get("Content");
	}

	public void setContent(String s) {
		this.content.strings.put("Content", s);
		this.updateModificationTime();
	}

	public String getContentSyntax() {
		return this.content.strings.get("ContentSyntax");
	}

	public void setContentSyntax(String s) {
		this.content.strings.put("ContentSyntax", s);
		this.updateModificationTime();
	}

	public boolean isDraft() {
		if(!this.content.booleans.containsKey("IsDraft")) {
			return Content.DEFAULT_DRAFT_STATUS;
		}
		return this.content.booleans.get("IsDraft");
	}

	public void setDraft(boolean b) {
		this.content.booleans.put("IsDraft", b);
	}

	public Date getContentCreationDate() {
		return new Date(this.content.longs.get("CreationDate"));
	}

	public Date getContentModificationDate() {
		return new Date(this.content.longs.get("LastModification"));
	}

	public Vector<String> getTags() {
		Vector<String> tags = new Vector<String>();
		for(String tag : this.content.stringsArrays.get("Tags")) {
			if(!tags.contains(tag)) {
				tags.add(tag);
			}
		}

		return tags;
	}

	public void setTags(Vector<String> s) {
		String[] tags = new String[s.size()];
		
		for(int i = 0; i < s.size(); ++i) {
			tags[i] = s.elementAt(i);
		}
		
		this.content.stringsArrays.put("Tags", tags);
		this.updateModificationTime();
	}

	public void putTag(String s) {
		Vector<String> tags = this.getTags();
		if(!tags.contains(s)) {
			tags.add(s);
		}
		
		this.setTags(tags);
		this.updateModificationTime();
	}

	public void deleteTag(String s) {
		Vector<String> tags = this.getTags();
		if(tags.contains(s)) {
			tags.remove(s);
		}

		this.setTags(tags);
		this.updateModificationTime();
	}
}
