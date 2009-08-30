/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper;

import freenet.client.async.DatabaseDisabledException;
import freenet.clients.http.PageMaker.THEME;
import freenet.l10n.BaseL10n;
import freenet.l10n.PluginL10n;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.PluginRespirator;
import freenet.pluginmanager.FredPluginBaseL10n;
import freenet.pluginmanager.FredPluginL10n;
import freenet.pluginmanager.FredPluginThemed;
import freenet.pluginmanager.FredPluginVersioned;
import freenet.pluginmanager.PluginStore;
import freenet.support.Logger;
import plugins.floghelper.ui.DataFormatter;
import plugins.floghelper.ui.FlogHelperToadlet;
import plugins.floghelper.ui.FlogListToadlet;

/**
 *
 * @author Artefact2
 */
public class FlogHelper implements FredPlugin, FredPluginThreadless, FredPluginBaseL10n, FredPluginL10n, FredPluginThemed, FredPluginVersioned {

	public static final short REVISION = 2;
	private static PluginRespirator pr;
	private static PluginL10n l10n;
	private static PluginStore store;

	public static BaseL10n getBaseL10n() {
		return FlogHelper.l10n.getBase();
	}

	public static PluginStore getStore() {
		return FlogHelper.store;
	}

	public static PluginRespirator getPR() {
		return FlogHelper.pr;
	}

	public static void putStore() {
		try {
			FlogHelper.pr.putStore(FlogHelper.store);
		} catch (DatabaseDisabledException ex) {
			Logger.error(FlogHelper.class, "Could not put PluginStore : " + ex.getMessage());
		}
	}

	public void terminate() {
		FlogHelper.pr.getPageMaker().removeNavigationCategory("FlogHelper");
	}

	public void runPlugin(final PluginRespirator pr) {
		FlogHelper.pr = pr;
		FlogHelper.l10n = new PluginL10n(this);
		try {
			FlogHelper.store = FlogHelper.pr.getStore();
		} catch (DatabaseDisabledException ex) {
			Logger.error(this.getClass(), "Could not load flogs from db4o - " + ex.getMessage());
		}

		FlogHelper.pr.getPageMaker().addNavigationCategory(FlogHelperToadlet.BASE_URI + "/",
				"FlogHelper", "FlogHelper", this);
		FlogHelper.pr.getToadletContainer().register(new FlogListToadlet(
				FlogHelper.pr.getHLSimpleClient()), "FlogHelper",
				FlogHelperToadlet.BASE_URI + "/", true, "FlogHelper",
				"FlogHelper", true, null);
	}

	public void setTheme(final THEME theme) {
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
		return "FlogHelper_UI_${lang}.override.l10n";
	}

	public ClassLoader getPluginClassLoader() {
		return FlogHelper.class.getClassLoader();
	}

	public String getVersion() {
		String rev = DataFormatter.formatIntLength(FlogHelper.REVISION, 4, false);
		return "r" + rev;
	}

	public String getString(String arg0) {
		return FlogHelper.getBaseL10n().getString(arg0);
	}
}
