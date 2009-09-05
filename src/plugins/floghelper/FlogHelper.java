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
import freenet.pluginmanager.FredPluginRealVersioned;
import freenet.pluginmanager.FredPluginTalker;
import freenet.pluginmanager.FredPluginThemed;
import freenet.pluginmanager.FredPluginVersioned;
import freenet.pluginmanager.PluginStore;
import freenet.support.Logger;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;
import plugins.floghelper.ui.ContentListToadlet;
import plugins.floghelper.ui.CreateOrEditContentToadlet;
import plugins.floghelper.ui.CreateOrEditFlogToadlet;
import plugins.floghelper.data.DataFormatter;
import plugins.floghelper.ui.ExportFlogToadlet;
import plugins.floghelper.ui.FlogHelperToadlet;
import plugins.floghelper.ui.FlogListToadlet;
import plugins.floghelper.ui.ImportFlogToadlet;

/**
 * TODO: proper GPL headers
 * TODO: proper javadoc
 * TODO: tags (store them as a String[] would do the trick)
 * TODO: render to xHTML (in a Map<String (filename), String (file contents)> in order not to leave any tracks on the HDD)
 * TODO: insert (maybe use the same SSK as the main WoT identity?)
 * TODO: javascript editbox
 * TODO: WikiCode first
 * TODO: WoT register context!
 * @author Artefact2
 */
public class FlogHelper implements FredPlugin, FredPluginThreadless, FredPluginBaseL10n, FredPluginL10n, FredPluginThemed, FredPluginVersioned, FredPluginRealVersioned, FredPluginTalker {

	public static final String PLUGIN_NAME = "FlogHelper";
	public static final int REVISION = 6;
	private static PluginRespirator pr;
	private static PluginL10n l10n;
	private static PluginStore store;
	private FlogListToadlet flogListToadlet;
	private CreateOrEditFlogToadlet createOrEditFlogToadlet;
	private ContentListToadlet contentListToadlet;
	private CreateOrEditContentToadlet createOrEditContentToadlet;
	private ExportFlogToadlet exportFlogToadlet;
	private ImportFlogToadlet importFlogToadlet;

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
		FlogHelper.pr.getPageMaker().removeNavigationCategory(FlogHelper.PLUGIN_NAME);
		FlogHelper.pr.getToadletContainer().unregister(this.flogListToadlet);
		FlogHelper.pr.getToadletContainer().unregister(this.createOrEditFlogToadlet);
		FlogHelper.pr.getToadletContainer().unregister(this.exportFlogToadlet);
		FlogHelper.pr.getToadletContainer().unregister(this.importFlogToadlet);
	}

	public void runPlugin(final PluginRespirator pr) {
		FlogHelper.pr = pr;
		FlogHelper.l10n = new PluginL10n(this);
		try {
			FlogHelper.store = FlogHelper.pr.getStore();
		} catch (DatabaseDisabledException ex) {
			Logger.error(this.getClass(), "Could not load flogs from db4o - " + ex.getMessage());
		}

		this.flogListToadlet = new FlogListToadlet(FlogHelper.pr.getHLSimpleClient());
		this.createOrEditFlogToadlet = new CreateOrEditFlogToadlet(FlogHelper.pr.getHLSimpleClient());
		this.contentListToadlet = new ContentListToadlet(FlogHelper.pr.getHLSimpleClient());
		this.createOrEditContentToadlet = new CreateOrEditContentToadlet(FlogHelper.pr.getHLSimpleClient());
		this.exportFlogToadlet = new ExportFlogToadlet(FlogHelper.pr.getHLSimpleClient());
		this.importFlogToadlet = new ImportFlogToadlet(FlogHelper.pr.getHLSimpleClient());

		FlogHelper.pr.getPageMaker().addNavigationCategory(FlogHelperToadlet.BASE_URI + "/",
				FlogHelper.PLUGIN_NAME, FlogHelper.PLUGIN_NAME, this);

		// The index page comes first, because everything will begin by "/",
		// and this will be parsed after every other toadlet.
		FlogHelper.pr.getToadletContainer().register(this.flogListToadlet, FlogHelper.PLUGIN_NAME,
				this.flogListToadlet.path(), true, FlogHelper.PLUGIN_NAME, FlogHelper.PLUGIN_NAME, true, null);

		FlogHelper.pr.getToadletContainer().register(this.createOrEditFlogToadlet, FlogHelper.PLUGIN_NAME,
				this.createOrEditFlogToadlet.path(), true, true);
		FlogHelper.pr.getToadletContainer().register(this.contentListToadlet, FlogHelper.PLUGIN_NAME,
				this.contentListToadlet.path(), true, true);
		FlogHelper.pr.getToadletContainer().register(this.createOrEditContentToadlet, FlogHelper.PLUGIN_NAME,
				this.createOrEditContentToadlet.path(), true, true);
		FlogHelper.pr.getToadletContainer().register(this.exportFlogToadlet, FlogHelper.PLUGIN_NAME,
				this.exportFlogToadlet.path(), true, true);
		FlogHelper.pr.getToadletContainer().register(this.importFlogToadlet, FlogHelper.PLUGIN_NAME,
				this.importFlogToadlet.path(), true, true);
	}

	public void setTheme(final THEME theme) {
	}

	public void setLanguage(final BaseL10n.LANGUAGE arg0) {
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

	public String getString(final String arg0) {
		return FlogHelper.getBaseL10n().getString(arg0);
	}

	public long getRealVersion() {
		return FlogHelper.REVISION;
	}

	public void onReply(String arg0, String arg1, SimpleFieldSet arg2, Bucket arg3) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
