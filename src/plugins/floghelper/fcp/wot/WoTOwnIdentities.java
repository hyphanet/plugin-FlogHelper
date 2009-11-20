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

	/**
	 * Get the nicknames of WoT identities. The map is <"ID", "Nickname (ID)">.
	 * @return Map of WoT identities.
	 * @throws PluginNotFoundException
	 */
	public static Map<String, String> getWoTIdentities() throws PluginNotFoundException {
		return getWoTIdentities("Nickname");
	}

	/**
	 * Get the request URI of a given author ID.
	 * @param author Author ID.
	 * @return Request URI of this identity.
	 */
	public static String getRequestURI(String author) {
		try {
			return getWoTIdentities("RequestURI").get(author);
		} catch (PluginNotFoundException ex) {
			return "**Error**";
		}
	}

	/**
	 * Get a specific field from WoT identities.
	 * @param field Field to get, eg "Nickname" or "InsertURI".
	 * @return Map of the requested data.
	 * @throws PluginNotFoundException
	 */
	public static Map<String, String> getWoTIdentities(final String field) throws PluginNotFoundException {
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
							} else if (s.startsWith(field)) {
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

	/**
	 * Sends a "Ping" FCP message to the WoT plugin.
	 * @throws PluginNotFoundException If the plugin is not loaded/not
	 * responding/buggy/whatever.
	 */
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
