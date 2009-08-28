/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper;

import freenet.clients.http.PageMaker.THEME;
import freenet.l10n.L10n.LANGUAGE;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginHTTP;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.PluginHTTPException;
import freenet.pluginmanager.PluginRespirator;
import freenet.pluginmanager.FredPluginL10n;
import freenet.pluginmanager.FredPluginThemed;
import freenet.support.api.HTTPRequest;
import plugins.floghelper.l10n.L10n;

/**
 *
 * @author Artefact2
 */
public class FlogHelper implements FredPlugin, FredPluginHTTP, FredPluginThreadless, FredPluginL10n, FredPluginThemed {
	private PluginRespirator pr;
	private THEME theme;


	public void terminate() {
		
	}

	public void runPlugin(final PluginRespirator pr) {
		this.pr = pr;
	}

	public String handleHTTPGet(final HTTPRequest request) throws PluginHTTPException {
		return this.getString("HelloWorldTest");
	}

	public String handleHTTPPost(final HTTPRequest request) throws PluginHTTPException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String getString(final String str) {
		return L10n.getString(str);
	}

	public void setLanguage(final LANGUAGE lang) {
		L10n.setLanguage(lang);
	}

	public void setTheme(final THEME theme) {
		this.theme = theme;
	}

}
