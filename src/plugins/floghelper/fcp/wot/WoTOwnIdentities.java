/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.fcp.wot;

import freenet.node.FSParseException;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.support.Logger;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import plugins.floghelper.fcp.ReceptorCore;
import plugins.floghelper.fcp.SyncPluginTalker;

/**
 * Used to get WoT identities.
 *
 * @author Artefact2
 */
public class WoTOwnIdentities {

	public static Map<String, String> getWoTIdentities() throws PluginNotFoundException {
		final HashMap<String, String> identities = new HashMap<String, String>();
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "GetOwnIdentities");

		SyncPluginTalker spt = new SyncPluginTalker(new ReceptorCore() {

			public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
				try {
					if (params.getString("Message").equals("OwnIdentities")) {
						Vector<String> identifiers = new Vector<String>();
						Vector<String> nicknames = new Vector<String>();
						for (final String s : params.toOrderedString().split("\n")) {
							if (s.startsWith("Identity")) {
								identifiers.add(s.split("=")[1]);
							} else if (s.startsWith("Nickname")) {
								nicknames.add(s.split("=")[1]);
							}
						}

						assert (identifiers.size() == nicknames.size());

						for (int i = 0; i < identifiers.size(); ++i) {
							identities.put(identifiers.get(i), nicknames.get(i) + " (" + identifiers.get(i) + ")");
						}
					} else {
						Logger.error(this, "Unexpected message : " + params.getString("Message"));
					}
				} catch (FSParseException ex) {
					Logger.error(this, "WoTOwnIdentities : Parse error !");
				}
			}
		}, sfs, null);

		spt.run();

		return identities;
	}

	public static void sendPing() throws PluginNotFoundException {
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "Ping");

		SyncPluginTalker spt = new SyncPluginTalker(new ReceptorCore() {

			public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
			}
		}, sfs, null);

		spt.run();
	}
}
