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
