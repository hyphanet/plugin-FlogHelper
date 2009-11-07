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
import java.util.Vector;
import plugins.floghelper.contentsyntax.js.JavascriptFactoryToadlet;
import plugins.floghelper.ui.ContentListToadlet;
import plugins.floghelper.ui.CreateOrEditContentToadlet;
import plugins.floghelper.ui.CreateOrEditFlogToadlet;
import plugins.floghelper.data.DataFormatter;
import plugins.floghelper.ui.ExportFlogToadlet;
import plugins.floghelper.ui.FlogHelperToadlet;
import plugins.floghelper.ui.FlogListToadlet;
import plugins.floghelper.ui.ImportFlogToadlet;
import plugins.floghelper.ui.PreviewToadlet;

/**
 * FlogHelper goals : lightweight, integrated in the node, SECURE BY DEFAULT,
 * don't recreate the wheel every time.
 *
 * This is the entry point of the plugin.
 *
 * TODO: proper GPL headers
 * TODO: proper javadoc
 * TODO: WoT register context!
 * TODO: filters
 * TODO: sorting (by creation date first)
 * TODO: maybe bundle the flog's index when inserting to allow searching using Librarian
 * TODO: maybe a caendar view for archives like Dotclear/WP
 * @author Artefact2
 */
public class FlogHelper implements FredPlugin, FredPluginThreadless, FredPluginBaseL10n, FredPluginL10n, FredPluginThemed, FredPluginVersioned, FredPluginRealVersioned, FredPluginTalker {

	public static final String PLUGIN_NAME = "FlogHelper";
	/**
	 * Don't forget to bump this when a new release is up.
	 */
	public static final int REVISION = 6;
	private static PluginRespirator pr;
	private static PluginL10n l10n;
	private static PluginStore store;
	/**
	 * Every toadlet should be in this vector, a loop registers them with the node.
	 */
	private final Vector<FlogHelperToadlet> myToadlets = new Vector<FlogHelperToadlet>();

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

	/**
	 * This code is executed when the user unloads the plugin.
	 * We unregister the toadlets.
	 * FIXME maybe we need to clean other stuff...
	 */
	public void terminate() {
		FlogHelper.pr.getPageMaker().removeNavigationCategory(FlogHelper.PLUGIN_NAME);
		for(FlogHelperToadlet e : this.myToadlets) {
			FlogHelper.pr.getToadletContainer().unregister(e);
		}
	}

	/**
	 * This code is run when the user loads the plugin.
	 * @param pr PluginRespirator to use.
	 */
	public void runPlugin(final PluginRespirator pr) {
		FlogHelper.pr = pr;
		FlogHelper.l10n = new PluginL10n(this);
		try {
			FlogHelper.store = FlogHelper.pr.getStore();
		} catch (DatabaseDisabledException ex) {
			// FIXME we need to handle this error gracefully !
			Logger.error(this.getClass(), "Could not load flogs from db4o - " + ex.getMessage());
		}

		this.myToadlets.add(new FlogListToadlet(FlogHelper.pr.getHLSimpleClient()));
		this.myToadlets.add(new CreateOrEditFlogToadlet(FlogHelper.pr.getHLSimpleClient()));
		this.myToadlets.add(new ContentListToadlet(FlogHelper.pr.getHLSimpleClient()));
		this.myToadlets.add(new CreateOrEditContentToadlet(FlogHelper.pr.getHLSimpleClient()));
		this.myToadlets.add(new ExportFlogToadlet(FlogHelper.pr.getHLSimpleClient()));
		this.myToadlets.add(new ImportFlogToadlet(FlogHelper.pr.getHLSimpleClient()));
		this.myToadlets.add(new PreviewToadlet(FlogHelper.pr.getHLSimpleClient()));
		this.myToadlets.add(new JavascriptFactoryToadlet(FlogHelper.pr.getHLSimpleClient()));

		FlogHelper.pr.getPageMaker().addNavigationCategory(FlogHelperToadlet.BASE_URI + "/",
				FlogHelper.PLUGIN_NAME, FlogHelper.PLUGIN_NAME, this);

		// The index page comes first, because everything will begin by "/",
		// and this will be parsed after every other toadlet.
		FlogHelper.pr.getToadletContainer().register(this.myToadlets.elementAt(0), FlogHelper.PLUGIN_NAME,
				this.myToadlets.elementAt(0).path(), true, FlogHelper.PLUGIN_NAME, FlogHelper.PLUGIN_NAME, true, null);

		for(int i = 1; i < this.myToadlets.size(); ++i) {
			FlogHelperToadlet e = this.myToadlets.elementAt(i);
			FlogHelper.pr.getToadletContainer().register(e, FlogHelper.PLUGIN_NAME,
				e.path(), true, true);
		}
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
