/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.data;

import freenet.support.Logger;
import freenet.support.api.HTTPUploadedFile;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 *
 * @author Artefact2
 */
public class Activelink {

	public static final int WIDTH = 108;
	public static final int HEIGHT = 36;
	public static final String MIMETYPE = "image/png";

	public static byte[] getByteArrayFromUploadedFile(HTTPUploadedFile activelink) {
		byte[] bytesAL = new byte[(int) activelink.getData().size()];

		try {
			BufferedInputStream is = new BufferedInputStream(activelink.getData().getInputStream());
			int b;
			int i = 0;
			while((b = is.read()) != -1) {
				bytesAL[i] = (byte)b;
				++i;
			}
		} catch (IOException ex) {
			Logger.error(Activelink.class, "Could not read activelink bucket - " + ex.getMessage());
		}

		return bytesAL;
	}
}
