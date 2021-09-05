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

import freenet.pluginmanager.FredPluginTalker;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * This receptor can read FCP replies back.
 * 
 * @author Artefact2
 */
public class PluginReceptor implements FredPluginTalker {

	/**
	 * Our ReceptorCore is basically a callback to handle the stuff when the plugin sends
	 * back a FCP response.
	 */
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
