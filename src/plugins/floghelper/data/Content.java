/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
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
	abstract public Date getContentModificationDate();

	abstract public Vector<String> getTags();
	abstract public void setTags(Vector<String> s);
	abstract public void putTag(String s);
	abstract public void deleteTag(String s);
}
