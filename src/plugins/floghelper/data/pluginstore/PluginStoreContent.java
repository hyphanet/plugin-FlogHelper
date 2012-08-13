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
		this.setContentSyntax("YAWKL");
		this.setDraft(Content.DEFAULT_DRAFT_STATUS);
		this.setTitle("Unnamed new entry");
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
	
	@Override
	public void setContentCreationDate(Date date) {
		this.content.longs.put("CreationDate", date.getTime());
	}

	public Date getContentModificationDate() {
		return new Date(this.content.longs.get("LastModification"));
	}
	
	@Override
	public void setContentModificationDate(Date date) {
		this.content.longs.put("LastModification", date.getTime());
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
