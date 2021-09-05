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

import freenet.support.Logger;
import freenet.support.api.HTTPUploadedFile;
import freenet.support.io.BucketTools;
import java.io.IOException;

/**
 * Provides some useful Activelink-related stuff.
 *
 * @author Artefact2
 */
public class Activelink {

	/**
	 * Width of an Activelink.
	 */
	public static final int WIDTH = 108;
	/**
	 * Height of an activelink.
	 */
	public static final int HEIGHT = 36;
	/**
	 * MIME type of an activelink.
	 */
	public static final String MIMETYPE = "image/png";

	/**
	 * Get a byte array from an uploaded file.
	 *
	 * @param activelink Matching HTTPUploadedFile
	 * @return File bytes.
	 */
	public static byte[] getByteArrayFromUploadedFile(HTTPUploadedFile activelink) {
		byte[] bytesAL = null;

		try {
			bytesAL = BucketTools.toByteArray(activelink.getData());
		} catch (IOException ex) {
			Logger.error(Activelink.class, "Could not read activelink bucket - " + ex.getMessage());
		}

		return bytesAL;
	}
}
