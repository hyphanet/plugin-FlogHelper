/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.data;

import java.util.Date;

/**
 * Abstract attachment representation.
 *
 * @author Artefact2
 */
public abstract class Attachment {
	abstract public Flog getParent();
	
	abstract public String getID();
	abstract public String getName();
	abstract public void   setName(String s);

	abstract public Date getInsertionDate();

	abstract public byte[] getData();
}
