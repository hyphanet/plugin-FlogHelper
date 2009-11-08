/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.ui;

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginStore;
import freenet.support.HTMLNode;
import freenet.support.MultiValueTable;
import freenet.support.api.HTTPRequest;
import java.io.IOException;
import java.net.URI;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.data.DataFormatter;

/**
 * This toadlet let the user export flogs.
 * 
 * @author Artefact2
 */
public class ExportFlogToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/Export/";

	public ExportFlogToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	public void getPageGet(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		this.getPagePost(pageNode, uri, request, ctx);
	}

	public void getPagePost(final PageNode pageNode, final URI uri, HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final PluginStore flog = FlogHelper.getStore().subStores.get(this.getParameterWhetherItIsPostOrGet(request, "FlogID", 7));
		if (flog == null) {
			this.sendErrorPage(ctx, 404, "Not found", "Incorrect or missing FlogID.");
			return;
		}

		if (request.isPartSet("Download")) {
			MultiValueTable<String, String> headers = new MultiValueTable<String, String>();
			headers.put("Content-Disposition", "attachment; filename=\"" + "Flog-" + flog.strings.get("ID") + ".backup.db4o" + '"');
			headers.put("Cache-Control", "private");
			headers.put("Content-Transfer-Encoding", "binary");
			byte[] data = flog.exportStore();
			ctx.sendReplyHeaders(200, "OK", headers, "application/octet-stream", data.length);
			ctx.writeData(data);
		} else {

			HTMLNode warning = this.getPM().getInfobox("infobox-alert", FlogHelper.getBaseL10n().getString("FlogExportWarning"), pageNode.content);
			warning.addChild("p", FlogHelper.getBaseL10n().getString("FlogExportWarningLong"));

			HTMLNode dlBox = this.getPM().getInfobox("infobox-minor", FlogHelper.getBaseL10n().getString("DownloadFlogBackup"), pageNode.content);
			HTMLNode dlForm = ctx.addFormChild(dlBox, this.path(), "DownloadFlogBackup");
			dlForm.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "Download", FlogHelper.getBaseL10n().getString("DownloadFlogBackupLong")});
			dlForm.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.strings.get("ID"))});
			writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
		}
	}
}
