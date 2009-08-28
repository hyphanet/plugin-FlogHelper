/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper;

import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginHTTP;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.PluginHTTPException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.api.HTTPRequest;

/**
 *
 * @author Artefact2
 */
public class FlogHelper implements FredPlugin, FredPluginHTTP, FredPluginThreadless {
	private PluginRespirator pr;


	public void terminate() {
		
	}

	public void runPlugin(PluginRespirator pr) {
		this.pr = pr;
	}

	public String handleHTTPGet(HTTPRequest request) throws PluginHTTPException {
		return "Hello world !";
	}

	public String handleHTTPPost(HTTPRequest request) throws PluginHTTPException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
