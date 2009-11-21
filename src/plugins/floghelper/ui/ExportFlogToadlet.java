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
package plugins.floghelper.ui;

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.HTMLNode;
import freenet.support.MultiValueTable;
import freenet.support.api.HTTPRequest;
import java.io.IOException;
import java.net.URI;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.data.DataFormatter;
import plugins.floghelper.data.Flog;
import plugins.floghelper.data.pluginstore.PluginStoreFlog;

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
		final Flog flog = new PluginStoreFlog(this.getParameterWhetherItIsPostOrGet(request, "FlogID", 7));

		if (request.isPartSet("Download")) {
			MultiValueTable<String, String> headers = new MultiValueTable<String, String>();
			headers.put("Content-Disposition", "attachment; filename=\"" + "Flog-" + flog.getID() + ".backup.db4o" + '"');
			headers.put("Cache-Control", "private");
			headers.put("Content-Transfer-Encoding", "binary");
			byte[] data = flog.exportFlog();
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
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.getID())});
			writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
		}
	}
}
