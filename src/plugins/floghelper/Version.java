/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper;

/**
 * This works the same way as the node version system.
 * 
 * @author Artefact2
 */
public class Version {
	/**
	 * This will be parsed by ant at compile time.
	 */
	private static final String gitRevision = "@custom@";

	public static String getRevision() {
		return Version.gitRevision;
	}
}
