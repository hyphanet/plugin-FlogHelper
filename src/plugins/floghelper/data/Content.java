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

import java.util.Date;
import java.util.Vector;

/**
 * Abstract content representation.
 *
 * @author Artefact2
 */
public abstract class Content {

	/**
	 * Should new contents be drafts by default ?
	 */
	public static final boolean DEFAULT_DRAFT_STATUS = false;

	abstract public Flog getParent();

	abstract public String getID();
	abstract public String getTitle();
	abstract public void   setTitle(String s);

	abstract public String getContent();
	abstract public void   setContent(String s);
	abstract public String getContentSyntax();
	abstract public void   setContentSyntax(String s);

	abstract public boolean isDraft();
	abstract public void setDraft(boolean b);

	abstract public Date getContentCreationDate();
	abstract public void setContentCreationDate(Date date);
	
	abstract public Date getContentModificationDate();
	abstract public void setContentModificationDate(Date date);

	abstract public Vector<String> getTags();
	abstract public void setTags(Vector<String> s);
	abstract public void putTag(String s);
	abstract public void deleteTag(String s);
}
