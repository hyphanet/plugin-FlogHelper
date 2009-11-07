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
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import plugins.floghelper.FlogHelper;

/**
 *
 * @author Artefact2
 */
public class ContentListToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/Flog/";

	public ContentListToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	public void getPageGet(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final PluginStore flog = FlogHelper.getStore().subStores.get(this.getParameterWhetherItIsPostOrGet(request, "FlogID", 7));
		if (flog == null) {
			this.sendErrorPage(ctx, 404, "Not found", "Incorrect or missing FlogID.");
			return;
		}

		final HTMLNode table = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("ContentListOf").replace("${FlogName}",
				DataFormatter.htmlSpecialChars(flog.strings.get("Title"))), pageNode.content).addChild("table");

		final HTMLNode tHead = table.addChild("thead");
		final HTMLNode tFoot = table.addChild("tfoot");
		final HTMLNode tBody = table.addChild("tbody");

		final HTMLNode actionsRow = new HTMLNode("tr");

		final HTMLNode formBack = FlogHelper.getPR().addFormChild(actionsRow.addChild("th", "colspan", "2"), FlogHelperToadlet.BASE_URI +
				FlogListToadlet.MY_URI, "BackToFlogList");
		formBack.addAttribute("method", "get");
		formBack.addChild("input", new String[]{"type", "value"},
				new String[]{"submit", FlogHelper.getBaseL10n().getString("BackToFlogList")});

		final HTMLNode formCreateNew = FlogHelper.getPR().addFormChild(actionsRow.addChild("th", "colspan", "5"), FlogHelperToadlet.BASE_URI +
				CreateOrEditContentToadlet.MY_URI, "CreateNewContent");
		formCreateNew.addAttribute("method", "get");
		formCreateNew.addChild("input", new String[]{"type", "value"},
				new String[]{"submit", FlogHelper.getBaseL10n().getString("CreateContent")});
		formCreateNew.addChild("input", new String[]{"type", "name", "value"},
				new String[]{"hidden", "FlogID", DataFormatter.toString(flog.strings.get("ID"))});

		final HTMLNode headersRow = new HTMLNode("tr");
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("ID"));
		headersRow.addChild("th", DataFormatter.htmlSpecialChars(FlogHelper.getBaseL10n().getString("Title")));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("CreationDate"));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("LastModification"));
		headersRow.addChild("th", "colspan", "3", FlogHelper.getBaseL10n().getString("Actions"));

		tHead.addChild(actionsRow);
		tHead.addChild(headersRow);
		tFoot.addChild(headersRow);

		if (flog.subStores.isEmpty()) {
			tBody.addChild("tr").addChild("td", "colspan", "7", FlogHelper.getBaseL10n().getString("NoContentsYet"));
		}

		for (final PluginStore content : flog.subStores.values()) {
			final HTMLNode row = tBody.addChild("tr");
			row.addChild("td").addChild("pre", DataFormatter.toString(content.strings.get("ID")));
			row.addChild("td", DataFormatter.toString(DataFormatter.htmlSpecialChars(content.strings.get("Title"))));
			row.addChild("td", DataFormatter.toString(new Date(content.longs.get("CreationDate")).toString()));
			row.addChild("td", DataFormatter.toString(new Date(content.longs.get("LastModification")).toString()));

			final HTMLNode formDetails = FlogHelper.getPR().addFormChild(row.addChild("td"), FlogHelperToadlet.BASE_URI +
					PreviewToadlet.MY_URI + flog.strings.get("ID") + "/Content-" + content.strings.get("ID") + ".html", "ContentDetails-" + content.strings.get("ID"));
			formDetails.addAttribute("method", "get");
			formDetails.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Preview")});

			final HTMLNode formDelete = FlogHelper.getPR().addFormChild(row.addChild("td"), this.path(),
					"DeleteContent-" + content.strings.get("ID"));
			formDelete.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Delete")});
			formDelete.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "ContentToDelete", DataFormatter.toString(content.strings.get("ID"))});
			formDelete.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.strings.get("ID"))});

			final HTMLNode formEdit = FlogHelper.getPR().addFormChild(row.addChild("td"), FlogHelperToadlet.BASE_URI +
					CreateOrEditContentToadlet.MY_URI, "EditContent-" + content.strings.get("ID"));
			formEdit.addAttribute("method", "get");
			formEdit.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Edit")});
			formEdit.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.strings.get("ID"))});
			formEdit.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "ContentID", DataFormatter.toString(content.strings.get("ID"))});
		}

		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}

	public void getPagePost(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final PluginStore flog = FlogHelper.getStore().subStores.get(this.getParameterWhetherItIsPostOrGet(request, "FlogID", 7));
		if (flog == null) {
			this.sendErrorPage(ctx, 404, "Not found", "Incorrect or missing FlogID.");
		}

		final String idToDelete = request.getPartAsString("ContentToDelete", 7);
		final String idToReallyDelete = request.getPartAsString("ContentToReallyDelete", 7);

		if (idToReallyDelete != null && !idToReallyDelete.equals("")) {
			if (request.getPartAsString("Yes", 3).equals("Yes")) {

				flog.subStores.remove(idToReallyDelete);
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
			form.addChild("p", FlogHelper.getBaseL10n().getString("ReallyDeleteContentLong").replace("${ContentID}", idToDelete));
			final HTMLNode buttons = form.addChild("p");
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "ContentToReallyDelete", idToDelete});
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", flog.strings.get("ID")});
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "Yes", FlogHelper.getBaseL10n().getString("Yes")});
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "No", FlogHelper.getBaseL10n().getString("No")});

			writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
			return;
		}

		this.handleMethodGET(uri, request, ctx);
	}
}
