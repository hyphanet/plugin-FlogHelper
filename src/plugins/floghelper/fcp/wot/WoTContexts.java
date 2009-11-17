/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.fcp.wot;

import freenet.pluginmanager.PluginNotFoundException;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;
import plugins.floghelper.fcp.ReceptorCore;
import plugins.floghelper.fcp.SyncPluginTalker;

/**
 * Used to manage WoT contexts and properties.
 *
 * @author Artefact2
 */
public class WoTContexts {
	public static final String FLOGHELPER_CONTEXT = "Flog";

	public static void addContext(String authorID) throws PluginNotFoundException {
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "AddContext");
		sfs.putOverwrite("Identity", authorID);
		sfs.putOverwrite("Context", WoTContexts.FLOGHELPER_CONTEXT);

		SyncPluginTalker spt = new SyncPluginTalker(new ReceptorCore() {

			public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
			}
		}, sfs, null);

		spt.run();
	}

	public static void addProperty(String authorID, String propertyName, String propertyValue) throws PluginNotFoundException {
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "SetProperty");
		sfs.putOverwrite("Identity", authorID);
		sfs.putOverwrite("Property", propertyName);
		sfs.putOverwrite("Value", propertyValue);

		SyncPluginTalker spt = new SyncPluginTalker(new ReceptorCore() {

			public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
			}
		}, sfs, null);

		spt.run();
	}
}
