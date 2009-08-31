/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.fcp;

import freenet.pluginmanager.FredPluginTalker;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 *
 * @author Artefact2
 */
public class PluginReceptor implements FredPluginTalker {

	private final ReceptorCore c;
	private boolean isOver = false;
	private boolean isStarted = false;

	public PluginReceptor(final ReceptorCore c) {
		this.c = c;
	}

	public void onReply(String arg0, String arg1, SimpleFieldSet arg2, Bucket arg3) {
		this.isStarted = true;
		this.c.onReply(arg0, arg1, arg2, arg3);
		this.isOver = true;
	}

	public boolean isStarted() {
		return this.isStarted;
	}

	public boolean isOver() {
		return this.isOver;
	}
}
