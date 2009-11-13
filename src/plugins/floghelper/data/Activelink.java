/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.data;

import freenet.support.Logger;
import freenet.support.api.HTTPUploadedFile;
import java.io.BufferedInputStream;
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
			bytesAL = DataFormatter.readAllBytesFromStream(activelink.getData().getInputStream(), (int)activelink.getData().size());
		} catch (IOException ex) {
			Logger.error(Activelink.class, "Could not read activelink bucket - " + ex.getMessage());
		}

		return bytesAL;
	}
}
