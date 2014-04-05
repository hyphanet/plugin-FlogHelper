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
package plugins.floghelper;

import com.db4o.ObjectContainer;
import freenet.client.async.DatabaseDisabledException;
import freenet.client.async.USKManager;
import freenet.clients.http.PageMaker.THEME;
import freenet.keys.USK;
import freenet.l10n.BaseL10n;
import freenet.l10n.PluginL10n;
import freenet.node.RequestClient;
import freenet.node.fcp.FCPMessage;
import freenet.node.useralerts.UserAlert;
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
import freenet.support.HTMLNode;
import freenet.support.Logger;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;
import plugins.floghelper.contentsyntax.js.JavascriptFactoryToadlet;
import plugins.floghelper.data.Flog;
import plugins.floghelper.data.pluginstore.PluginStoreFlog;
import plugins.floghelper.ui.ContentListToadlet;
import plugins.floghelper.ui.CreateOrEditContentToadlet;
import plugins.floghelper.ui.CreateOrEditFlogToadlet;
import plugins.floghelper.ui.AttachmentsToadlet;
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
 * @author Artefact2
 */
public class FlogHelper implements FredPlugin, FredPluginThreadless, FredPluginBaseL10n, FredPluginL10n, FredPluginThemed, FredPluginVersioned, FredPluginRealVersioned, FredPluginTalker {

	public static final String l10nFilesBasePath = "plugins/floghelper/l10n/";
	public static final String l10nFilesMask = "UI_${lang}.l10n";
	public static final String l10nOverrideFilesMask = "FlogHelper_UI_${lang}.override.l10n";
	private static String PLUGIN_NAME;
	private static PluginRespirator pr;
	private static PluginL10n l10n;
	private static PluginStore store;
	private static USKManager uskManager;
	/**
	 * Every toadlet should be in this vector, a loop registers them with the node.
	 */
	private final Vector<FlogHelperToadlet> myToadlets = new Vector<FlogHelperToadlet>();

	/**
	 * BaseL10n object can be accessed statically to get L10n data from anywhere.
	 *
	 * @return L10n object.
	 */
	public static BaseL10n getBaseL10n() {
		return FlogHelper.l10n.getBase();
	}

	/**
	 * Get the top-level PluginStored used by this plugin.
	 *
	 * @return Top-level PluginStore.
	 */
	public static PluginStore getStore() {
		return FlogHelper.store;
	}

	/**
	 * Get the PluginRespirator given in runPlugin().
	 *
	 * @return PluginRespirator used by the plugin.
	 */
	public static PluginRespirator getPR() {
		return FlogHelper.pr;
	}

	/**
	 * This indicates to the node that the Database must be commited. You must
	 * call this when you insert/delete/update stuff in the database or the changes
	 * won't be saved.
	 */
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
	 * This plugin is threadless so there's nothing to do here except
	 * unregister the toadlets...
	 */
	public void terminate() {
		this.unregisterToadlets();
		for(Flog f : PluginStoreFlog.getFlogs()) {
				try {
					FlogHelper.uskManager.unsubscribe(USK.create(f.getRequestURI()), f.getUSKCallback());
				} catch (Exception ex) {
					Logger.error(this, "", ex);
				}
			}
	}

	/**
	 * This code is run when the user loads the plugin.
	 * @param pr PluginRespirator to use.
	 */
	public void runPlugin(final PluginRespirator pr) {
		FlogHelper.pr = pr;
		FlogHelper.l10n = new PluginL10n(this);
		FlogHelper.PLUGIN_NAME = FlogHelper.getBaseL10n().getString("FlogHelper");
		FlogHelper.uskManager = FlogHelper.getPR().getNode().clientCore.uskManager;
		try {
			FlogHelper.store = FlogHelper.pr.getStore();
		} catch (DatabaseDisabledException ex) {
			Logger.error(this, "Could not load flogs from db4o - " + ex.getMessage());
			// We wait until we have a database to register toadlets
			while(!FlogHelper.pr.getNode().hasDatabase()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex1) {

				}
			}
		} finally {
			this.registerToadlets();
			this.subscribeToFlogUSKs();
			checkForDangerousFlogs();
		}
	}
	
	public static void generateWarnings(HTMLNode addTo) {
		List<PluginStoreFlog> flogs = PluginStoreFlog.getFlogs();
		for(final PluginStoreFlog flog : flogs) {
			if(flog.shouldPublishStoreDump()) {
				final String hostname = flog.taintedHostname();
				final String title = flog.getTitle();
				final String author = flog.getAuthorName();
				addTo.addChild("p").addChild("b", FlogHelper.getBaseL10n().getString("WarningPublishedDangerousFlog", new String[] { "title", "author", "hostname" }, new String[] { title, author, hostname } ));
			}
		}
	}

	private void checkForDangerousFlogs() {
		List<PluginStoreFlog> flogs = PluginStoreFlog.getFlogs();
		for(final PluginStoreFlog flog : flogs) {
			if(flog.shouldPublishStoreDump() && !flog.userWarnedTainted()) {
				// TODO: No way to tell whether it's been published or not AFAICS.
				// If it hasn't we could just turn it off.
				freenet.node.Node n = pr.getNode();
				final String hostname = flog.taintedHostname();
				final String title = flog.getTitle();
				final String author = flog.getAuthorName();
				n.clientCore.alerts.register(new UserAlert() {

					@Override
					public boolean userCanDismiss() {
						return true;
					}

					@Override
					public String getTitle() {
						return FlogHelper.getBaseL10n().getString("WarningPublishedDangerousFlogTitle", new String[] { "title", "author", "hostname" }, new String[] { title, author, hostname } );
					}

					@Override
					public String getText() {
						return FlogHelper.getBaseL10n().getString("WarningPublishedDangerousFlog", new String[] { "title", "author", "hostname" }, new String[] { title, author, hostname } );
					}

					@Override
					public HTMLNode getHTMLText() {
						return new HTMLNode("#", getText());
					}

					@Override
					public String getShortText() {
						return FlogHelper.getBaseL10n().getString("WarningPublishedDangerousFlogTitle", new String[] { "title", "author", "hostname" }, new String[] { title, author, hostname } );
					}

					@Override
					public short getPriorityClass() {
						return UserAlert.CRITICAL_ERROR;
					}

					@Override
					public boolean isValid() {
						return flog.shouldPublishStoreDump() && !flog.userWarnedTainted();
					}

					@Override
					public void isValid(boolean validity) {
						if(!validity) {
							flog.userWarnedTainted(true);
							flog.putFlog();
							putStore();
						}
					}

					@Override
					public String dismissButtonText() {
						return FlogHelper.getBaseL10n().getString("TurnOffDangerousFlog");
					}

					@Override
					public boolean shouldUnregisterOnDismiss() {
						return true;
					}

					@Override
					public void onDismiss() {
						isValid(false);
					}

					@Override
					public String anchor() {
						return "floghelper-dangerous-flog-"+flog.getAuthorID()+"-"+flog.getID();
					}

					@Override
					public boolean isEventNotification() {
						return false;
					}

					@Override
					public FCPMessage getFCPMessage() {
						return null;
					}

					@Override
					public long getUpdatedTime() {
						return 0;
					}
					
				});
			}
		}
		// TODO Auto-generated method stub
		
	}

	/**
	 * Refresh the USK subscriptions of all flogs in the database.
	 */
	public static void subscribeToFlogUSKs() {
		for (Flog f : PluginStoreFlog.getFlogs()) {
			try {
				final USK uri = USK.create(f.getRequestURI());
				FlogHelper.uskManager.unsubscribe(uri, f.getUSKCallback());
				FlogHelper.uskManager.subscribe(uri, f.getUSKCallback(), true, new RequestClient() {

					public boolean persistent() {
						return false;
					}

					public void removeFrom(ObjectContainer arg0) {
					}

					public boolean realTimeFlag() {
						return false;
					}
				});
			} catch (Exception ex) {
				Logger.error(FlogHelper.class, "", ex);
			}
		}
	}

	/**
	 * Register toadlets in the main menu.
	 */
	private void registerToadlets() {
		this.myToadlets.clear();

		this.myToadlets.add(new FlogListToadlet(FlogHelper.pr.getHLSimpleClient()));
		this.myToadlets.add(new CreateOrEditFlogToadlet(FlogHelper.pr.getHLSimpleClient()));
		this.myToadlets.add(new ContentListToadlet(FlogHelper.pr.getHLSimpleClient()));
		this.myToadlets.add(new CreateOrEditContentToadlet(FlogHelper.pr.getHLSimpleClient()));
		this.myToadlets.add(new ExportFlogToadlet(FlogHelper.pr.getHLSimpleClient()));
		this.myToadlets.add(new ImportFlogToadlet(FlogHelper.pr.getHLSimpleClient()));
		this.myToadlets.add(new PreviewToadlet(FlogHelper.pr.getHLSimpleClient()));
		this.myToadlets.add(new JavascriptFactoryToadlet(FlogHelper.pr.getHLSimpleClient()));
		this.myToadlets.add(new AttachmentsToadlet(FlogHelper.pr.getHLSimpleClient()));

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

	/**
	 * Unregister all the registered toadlets in the main menu.
	 */
	private void unregisterToadlets() {
		FlogHelper.pr.getPageMaker().removeNavigationCategory(FlogHelper.PLUGIN_NAME);
		for(FlogHelperToadlet e : this.myToadlets) {
			FlogHelper.pr.getToadletContainer().unregister(e);
		}
	}

	/**
	 * This is pretty much useless because we use toadlets.
	 * @param theme
	 */
	public void setTheme(final THEME theme) {
	}

	/**
	 * This code is only called during startup or when the user
	 * selects another language in the UI.
	 * @param arg0 Language to use.
	 */
	public void setLanguage(final BaseL10n.LANGUAGE arg0) {
		FlogHelper.l10n = new PluginL10n(this, arg0);
	}

	/**
	 * This is where our L10n files are stored.
	 * @return Path of our L10n files.
	 */
	public String getL10nFilesBasePath() {
		return FlogHelper.l10nFilesBasePath;
	}

	/**
	 * This is the mask of our L10n files : UI_en.l10n, UI_fr.10n, ...
	 * @return Mask of the L10n files.
	 */
	public String getL10nFilesMask() {
		return FlogHelper.l10nFilesMask;
	}

	/**
	 * Override L10n files are stored on the disk, their names should be explicit
	 * we put here the plugin name, and the "override" indication. Plugin L10n
	 * override is not implemented in the node yet.
	 * @return Mask of the override L10n files.
	 */
	public String getL10nOverrideFilesMask() {
		return FlogHelper.l10nOverrideFilesMask;
	}

	/**
	 * Get the ClassLoader of this plugin. This is necessary when getting
	 * resources inside the plugin's Jar, for example L10n files.
	 * @return
	 */
	public ClassLoader getPluginClassLoader() {
		return FlogHelper.class.getClassLoader();
	}

	/**
	 * Get the formatted version of this plugin, for example "r0012" if revision 12.
	 * @return Formatted version.
	 */
	public String getVersion() {
		return Version.getVersion();
	}

	/**
	 * Get the (localized) name of the plugin.
	 *
	 * @return Name of the plugin
	 */
	public static String getName() {
		return FlogHelper.PLUGIN_NAME;
	}

	/**
	 * Get the revision of this plugin.
	 * @return Revision
	 */
	public long getRealVersion() {
		return Version.getRevision();
	}

	/**
	 * This code is never used, it's just there because we need to talk to WoT
	 * via FCP.
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void onReply(String arg0, String arg1, SimpleFieldSet arg2, Bucket arg3) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * This code is only used by FredPluginL10n...
	 * @param arg0
	 * @return
	 */
	public String getString(String arg0) {
		return FlogHelper.getBaseL10n().getString(arg0);
	}

	public static USKManager getUSKManager() {
		return FlogHelper.uskManager;
	}
}
