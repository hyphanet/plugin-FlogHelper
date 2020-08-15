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
package plugins.floghelper.fcp;

import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginTalker;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;
import plugins.floghelper.FlogHelper;

/**
 * This class handles FCP messages in a synchronous way. It simplifies the code
 * for toadlets.
 *
 * @author Artefact2
 */
public class SyncPluginTalker {

	/**
	 * Full classname of the WoT plugin.
	 */
	public final static String WOT_NAME = "plugins.WebOfTrust.WebOfTrust";
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
