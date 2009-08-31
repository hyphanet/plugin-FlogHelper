/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.fcp;

import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 *
 * @author Artefact2
 */
public interface ReceptorCore {

	public void onReply(String arg0, String arg1, SimpleFieldSet arg2, Bucket arg3);
}
