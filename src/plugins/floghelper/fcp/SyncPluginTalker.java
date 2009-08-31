/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.fcp;

import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginTalker;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;
import plugins.floghelper.FlogHelper;

/**
 *
 * @author Artefact2
 */
public class SyncPluginTalker {

	public final static String WOT_NAME = "plugins.WoT.WoT";
	private final PluginReceptor receptor;
	private final String to;
	private final PluginTalker talker;
	private final SimpleFieldSet data;
	private final Bucket data2;
	private final long millisTimeout;

	public SyncPluginTalker(final ReceptorCore receptor, final SimpleFieldSet data, final Bucket data2) throws PluginNotFoundException {
		this(receptor, WOT_NAME, data, data2);
	}

	public SyncPluginTalker(final ReceptorCore receptor, final String to, final SimpleFieldSet data, final Bucket data2) throws PluginNotFoundException {
		this(receptor, to, FlogHelper.class.getName(), data, data2);
	}

	public SyncPluginTalker(final ReceptorCore receptor, final String to, final String identifier, final SimpleFieldSet data, final Bucket data2) throws PluginNotFoundException {
		this(receptor, to, identifier, data, data2, 30000);
	}

	public SyncPluginTalker(final ReceptorCore receptorCore, final String to, final String identifier, final SimpleFieldSet data, final Bucket data2, final long millisTimeout) throws PluginNotFoundException {
		this.to = to;
		this.receptor = new PluginReceptor(receptorCore);
		this.talker = FlogHelper.getPR().getPluginTalker(this.receptor, this.to, identifier);
		this.data = data;
		this.data2 = data2;
		this.millisTimeout = millisTimeout;
	}

	public boolean run() {
		long start = System.currentTimeMillis();

		// This call is async
		this.talker.send(this.data, this.data2);

		// If the receptor don't recieve anything in the timeout period,
		// we return false
		while (!this.receptor.isStarted()) {
			if (System.currentTimeMillis() - start > this.millisTimeout) {
				return false;
			} else {
				try {
					Thread.sleep(15);
				} catch (InterruptedException ex) {
				}
			}
		}

		// We make sure that the receptor finished his stuff before
		// returning. This method is blocking !
		while (!this.receptor.isOver()) {
			try {
				Thread.sleep(15);
			} catch (InterruptedException ex) {
			}
		}

		return true;
	}
}
