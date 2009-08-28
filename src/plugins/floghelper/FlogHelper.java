/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper;

import freenet.clients.http.PageMaker.THEME;
import freenet.l10n.BaseL10n;
import freenet.l10n.BaseL10n.LANGUAGE;
import freenet.l10n.PluginL10n;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginHTTP;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.PluginHTTPException;
import freenet.pluginmanager.PluginRespirator;
import freenet.pluginmanager.FredPluginBaseL10n;
import freenet.pluginmanager.FredPluginThemed;
import freenet.support.api.HTTPRequest;

/**
 *
 * @author Artefact2
 */
public class FlogHelper implements FredPlugin, FredPluginHTTP, FredPluginThreadless, FredPluginBaseL10n, FredPluginThemed {
	private PluginRespirator pr;
	private THEME theme;
	private static PluginL10n l10n;

	public static BaseL10n getBaseL10n() {
		return FlogHelper.l10n.getBase();
	}

	public void terminate() {
		
	}

	public void runPlugin(final PluginRespirator pr) {
		this.pr = pr;
		FlogHelper.l10n = new PluginL10n(this);
	}

	public String handleHTTPGet(final HTTPRequest request) throws PluginHTTPException {
		return FlogHelper.getBaseL10n().getString("HelloWorldTest");
	}

	public String handleHTTPPost(final HTTPRequest request) throws PluginHTTPException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setTheme(final THEME theme) {
		this.theme = theme;
	}

	public void setLanguage(BaseL10n.LANGUAGE arg0) {
		FlogHelper.l10n = new PluginL10n(this, arg0);
	}

	public String getL10nFilesBasePath() {
		return "plugins/floghelper/l10n/";
	}

	public String getL10nFilesMask() {
		return "UI_${lang}.l10n";
	}

	public String getL10nOverrideFilesMask() {
		return "UI_${lang}.override.l10n";
	}

	public ClassLoader getPluginClassLoader() {
		return FlogHelper.class.getClassLoader();
	}

}
