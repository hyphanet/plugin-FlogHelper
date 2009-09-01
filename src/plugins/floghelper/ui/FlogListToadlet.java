/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.ui;

import plugins.floghelper.data.DataFormatter;
import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginStore;
import freenet.support.Base64;
import freenet.support.HTMLNode;
import freenet.support.Logger;
import freenet.support.api.HTTPRequest;
import java.io.IOException;
import java.net.URI;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.data.Activelink;

/**
 *
 * @author Artefact2
 */
public class FlogListToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/";

	public FlogListToadlet(final HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	public void getPageGet(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final HTMLNode table = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("FlogList"), pageNode.content).addChild("table");

		final HTMLNode tHead = table.addChild("thead");
		final HTMLNode tFoot = table.addChild("tfoot");
		final HTMLNode tBody = table.addChild("tbody");

		final HTMLNode actionsRow = new HTMLNode("tr");

		final HTMLNode formCreateNew = FlogHelper.getPR().addFormChild(actionsRow.addChild("th", "colspan", "8"), FlogHelperToadlet.BASE_URI +
				CreateOrEditFlogToadlet.MY_URI, "CreateNewFlog");
		formCreateNew.addAttribute("method", "get");
		formCreateNew.addChild("input", new String[]{"type", "value"},
				new String[]{"submit", FlogHelper.getBaseL10n().getString("CreateFlog")});

		final HTMLNode headersRow = new HTMLNode("tr");
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

		for (final PluginStore flog : FlogHelper.getStore().subStores.values()) {
			final HTMLNode activelinkP = new HTMLNode("td");
			if(flog.bytesArrays.get("Activelink") != null) {
				HTMLNode activelinkImg = new HTMLNode("img");
				final String base64str = Base64.encodeStandard(flog.bytesArrays.get("Activelink"));
				activelinkImg.addAttribute("src", "data:" + Activelink.MIMETYPE + ";base64," + base64str);
				activelinkImg.addAttribute("width", Integer.toString(Activelink.WIDTH));
				activelinkImg.addAttribute("height", Integer.toString(Activelink.HEIGHT));
				activelinkImg.addAttribute("alt", FlogHelper.getBaseL10n().getString("ActivelinkAlt"));
				activelinkImg.addAttribute("style", "vertical-align: middle;");
				activelinkP.addChild(activelinkImg);
			}

			final HTMLNode row = tBody.addChild("tr");
			row.addChild("td").addChild("pre", DataFormatter.toString(flog.strings.get("ID")));
			row.addChild(activelinkP);
			row.addChild("td", DataFormatter.toString(flog.strings.get("Title")));
			row.addChild("td", DataFormatter.toString(flog.strings.get("SmallDescription")));
			row.addChild("td", DataFormatter.toString(flog.subStores.size()));

			final HTMLNode formDetails = FlogHelper.getPR().addFormChild(row.addChild("td"), FlogHelperToadlet.BASE_URI +
					ContentListToadlet.MY_URI, "FlogDetails-" + flog.strings.get("ID"));
			formDetails.addAttribute("method", "get");
			formDetails.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Details")});
			formDetails.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.strings.get("ID"))});

			final HTMLNode formDelete = FlogHelper.getPR().addFormChild(row.addChild("td"), this.path(),
					"DeleteFlog-" + flog.strings.get("ID"));
			formDelete.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Delete")});
			formDelete.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogToDelete", DataFormatter.toString(flog.strings.get("ID"))});

			final HTMLNode formEdit = FlogHelper.getPR().addFormChild(row.addChild("td"), FlogHelperToadlet.BASE_URI +
					CreateOrEditFlogToadlet.MY_URI, "EditFlog-" + flog.strings.get("ID"));
			formEdit.addAttribute("method", "get");
			formEdit.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Edit")});
			formEdit.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.strings.get("ID"))});
		}

		this.getPM().getInfobox("infobox-minor", "PluginStore Dump", pageNode.content).addChild("pre", DataFormatter.printStore(FlogHelper.getStore()));

		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}

	public void getPagePost(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final String idToDelete = request.getPartAsString("FlogToDelete", 7);
		final String idToReallyDelete = request.getPartAsString("FlogToReallyDelete", 7);

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

		if (idToDelete != null && !idToDelete.equals("")) {
			final HTMLNode confirm = this.getPM().getInfobox("infobox-alert", FlogHelper.getBaseL10n().getString("ReallyDelete"), pageNode.content);
			final HTMLNode form = FlogHelper.getPR().addFormChild(confirm, this.path(), "ReallyDelete-" + idToDelete);
			form.addChild("p", FlogHelper.getBaseL10n().getString("ReallyDeleteFlogLong").replace("${FlogID}", idToDelete));
			final HTMLNode buttons = form.addChild("p");
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
