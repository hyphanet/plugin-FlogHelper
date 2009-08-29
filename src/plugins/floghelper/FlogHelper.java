/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import freenet.client.async.ClientContext;
import freenet.client.async.DBJob;
import freenet.client.async.DatabaseDisabledException;
import freenet.clients.http.PageMaker.THEME;
import freenet.l10n.BaseL10n;
import freenet.l10n.PluginL10n;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginHTTP;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.PluginHTTPException;
import freenet.pluginmanager.PluginRespirator;
import freenet.pluginmanager.FredPluginBaseL10n;
import freenet.pluginmanager.FredPluginThemed;
import freenet.pluginmanager.FredPluginVersioned;
import freenet.support.Logger;
import freenet.support.api.HTTPRequest;
import freenet.support.io.NativeThread;
import java.util.Vector;
import plugins.floghelper.data.FlogContainer;
import plugins.floghelper.ui.DataFormatter;
import plugins.floghelper.ui.Page;

/**
 *
 * @author Artefact2
 */
public class FlogHelper implements FredPlugin, FredPluginHTTP, FredPluginThreadless, FredPluginBaseL10n, FredPluginThemed, FredPluginVersioned {
	public static final short REVISION = 1;

	private static PluginRespirator pr;
	private static PluginL10n l10n;
	private static Vector<FlogContainer> flogs;

	public static BaseL10n getBaseL10n() {
		return FlogHelper.l10n.getBase();
	}

	public static void loadFlogs() throws DatabaseDisabledException {
		FlogHelper.pr.getNode().clientCore.runBlocking(new DBJob() {

			public boolean run(ObjectContainer arg0, ClientContext arg1) {
				ObjectSet<Vector<FlogContainer>> data = arg0.queryByExample(new Vector<FlogContainer>().getClass());
				System.err.println("FlogContainers : " + data.size());
				if(data.size() > 0) {
					FlogHelper.flogs = data.get(data.size() - 1);
				} else FlogHelper.flogs = new Vector<FlogContainer>();
				return false;
			}
		}, NativeThread.MAX_PRIORITY);
	}

	public static void syncFlogs() throws DatabaseDisabledException {
		FlogHelper.pr.getNode().clientCore.queue(new DBJob() {

			public boolean run(ObjectContainer arg0, ClientContext arg1) {
				arg0.store(FlogHelper.flogs);
				arg0.commit();
				return false;
			}
		}, NativeThread.NORM_PRIORITY, false);
	}

	public static Vector<FlogContainer> getFlogs() {
		return FlogHelper.flogs;
	}

	public void terminate() {
		
	}

	public void runPlugin(final PluginRespirator pr) {
		FlogHelper.pr = pr;
		FlogHelper.l10n = new PluginL10n(this);
		try {
			FlogHelper.loadFlogs();
		} catch (DatabaseDisabledException ex) {
			Logger.error(this.getClass(), "Could not load flogs from db4o - " + ex.getMessage());
		}
	}

	public String handleHTTPGet(final HTTPRequest request) throws PluginHTTPException {
		return Page.handleHTTPGet(request);
	}

	public String handleHTTPPost(final HTTPRequest request) throws PluginHTTPException {
		return Page.handleHTTPPost(request);
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
		return "UI_${lang}.override.l10n";
	}

	public ClassLoader getPluginClassLoader() {
		return FlogHelper.class.getClassLoader();
	}

	public String getVersion() {
		String rev = DataFormatter.formatIntLength(FlogHelper.REVISION, 4, false);
		return "r" + rev;
	}

}
