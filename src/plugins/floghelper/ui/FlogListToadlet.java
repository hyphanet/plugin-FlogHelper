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
import freenet.support.api.HTTPRequest;
import java.io.IOException;
import java.net.URI;
import plugins.floghelper.FlogHelper;

/**
 *
 * @author romain
 */
public class FlogListToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/";

	public FlogListToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	public void handleMethodGET(URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		PageNode pageNode = FlogHelper.getPR().getPageMaker().getPageNode("FlogHelper", ctx);

		HTMLNode table = this.getPM().getInfobox(null, "FlogList", pageNode.content).addChild("table");

		HTMLNode tHead = table.addChild("thead");
		HTMLNode tFoot = table.addChild("tfoot");
		HTMLNode tBody = table.addChild("tbody");

		HTMLNode actionsRow = new HTMLNode("tr");

		HTMLNode formCreateNew = FlogHelper.getPR().addFormChild(actionsRow.addChild("th", "colspan", "8"), FlogHelperToadlet.BASE_URI +
				CreateOrEditFlogToadlet.MY_URI, "CreateNewFlog");
		formCreateNew.addChild("input", new String[]{"type", "value"},
				new String[]{"submit", FlogHelper.getBaseL10n().getString("CreateFlog")});

		HTMLNode headersRow = new HTMLNode("tr");
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("ID"));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("Activelink"));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("Title"));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("SmallDescription"));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("NumberOfEntries"));
		headersRow.addChild("th", "colspan", "3", FlogHelper.getBaseL10n().getString("Actions"));

		tHead.addChild(actionsRow);
		tHead.addChild(headersRow);
		tFoot.addChild(headersRow);

		if (FlogHelper.getStore().subStores.isEmpty()) {
			tBody.addChild("tr").addChild("td", "colspan", "8", FlogHelper.getBaseL10n().getString("NoFlogsYet"));
		}

		for (PluginStore flog : FlogHelper.getStore().subStores.values()) {
			HTMLNode row = tBody.addChild("tr");
			row.addChild("td", DataFormatter.toString(flog.strings.get("ID")));
			row.addChild("td", DataFormatter.toString(flog.strings.get("Activelink")));
			row.addChild("td", DataFormatter.toString(flog.strings.get("Title")));
			row.addChild("td", DataFormatter.toString(flog.strings.get("SmallDescription")));
			row.addChild("td", DataFormatter.toString(flog.subStores.size()));

			HTMLNode formDetails = FlogHelper.getPR().addFormChild(row.addChild("td"), FlogHelperToadlet.BASE_URI +
					// FIXME do not use hardcoded URI here
					"/Flog/" + flog.strings.get("ID"), "FlogDetails-" + flog.strings.get("ID"));
			formDetails.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Details")});
			formDetails.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.strings.get("ID"))});

			HTMLNode formDelete = FlogHelper.getPR().addFormChild(row.addChild("td"), this.path(),
					"DeleteFlog-" + flog.strings.get("ID"));
			formDelete.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Delete")});
			formDelete.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogToDelete", DataFormatter.toString(flog.strings.get("ID"))});

			HTMLNode formEdit = FlogHelper.getPR().addFormChild(row.addChild("td"), FlogHelperToadlet.BASE_URI +
					CreateOrEditFlogToadlet.MY_URI, "EditFlog-" + flog.strings.get("ID"));
			formEdit.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Edit")});
			formEdit.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.strings.get("ID"))});
		}

		this.getPM().getInfobox("infobox-minor", "PluginStore Dump", pageNode.content).addChild("pre", DataFormatter.printStore(FlogHelper.getStore()));

		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}

	public void handleMethodPOST(URI uri, HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		String idToDelete = request.getPartAsString("FlogToDelete", 7);
		String idToReallyDelete = request.getPartAsString("FlogToReallyDelete", 7);

		if (idToReallyDelete != null && !idToReallyDelete.equals("")) {
			if (request.getPartAsString("Yes", 3).equals("Yes")) {
				FlogHelper.getStore().subStores.remove(idToReallyDelete);
				FlogHelper.putStore();
				this.handleMethodGET(uri, request, ctx);
				return;
			} else {
				this.handleMethodGET(uri, request, ctx);
				return;
			}
		}

		PageNode pageNode = FlogHelper.getPR().getPageMaker().getPageNode("FlogHelper", ctx);

		if (idToDelete != null && !idToDelete.equals("")) {
			HTMLNode confirm = this.getPM().getInfobox("infobox-alert", FlogHelper.getBaseL10n().getString("ReallyDelete"), pageNode.content);
			HTMLNode form = FlogHelper.getPR().addFormChild(confirm, this.path(), "ReallyDelete-" + idToDelete);
			form.addChild("p", FlogHelper.getBaseL10n().getString("ReallyDeleteFlogLong").replace("${FlogID}", idToDelete));
			HTMLNode buttons = form.addChild("p");
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogToReallyDelete", idToDelete});
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "Yes", FlogHelper.getBaseL10n().getString("Yes")});
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "No", FlogHelper.getBaseL10n().getString("No")});
		}

		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}
}
