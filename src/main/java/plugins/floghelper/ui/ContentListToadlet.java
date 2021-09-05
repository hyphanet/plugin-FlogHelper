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

import plugins.floghelper.data.DataFormatter;
import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;
import java.io.IOException;
import java.net.URI;
import java.util.TreeMap;

import plugins.floghelper.FlogHelper;
import plugins.floghelper.data.Content;
import plugins.floghelper.data.Flog;
import plugins.floghelper.data.pluginstore.PluginStoreFlog;
import plugins.floghelper.ui.flog.FlogFactory;

/**
 * This toadlet shows a list of a given Flog's contents.
 * 
 * @author Artefact2
 */
public class ContentListToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/Flog/";

	public ContentListToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	public void getPageGet(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final Flog flog = new PluginStoreFlog(this.getParameterWhetherItIsPostOrGet(request, "FlogID", 7));

		final HTMLNode table = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("ContentListOf").replace("${FlogName}",
				flog.getTitle()), pageNode.content).addChild("table");

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
				new String[]{"hidden", "FlogID", DataFormatter.toString(flog.getID())});

		final HTMLNode headersRow = new HTMLNode("tr");
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("ID"));
		headersRow.addChild("th", DataFormatter.htmlSpecialChars(FlogHelper.getBaseL10n().getString("Title")));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("CreationDate") + " \u25BC");
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("LastModification"));
		headersRow.addChild("th", "colspan", "3", FlogHelper.getBaseL10n().getString("Actions"));

		tHead.addChild(actionsRow);
		tHead.addChild(headersRow);
		tFoot.addChild(headersRow);

		if (flog.getNumberOfContents() == 0) {
			tBody.addChild("tr").addChild("td", "colspan", "7", FlogHelper.getBaseL10n().getString("NoContentsYet"));
		}

		// Let's sort the contents by descending creation date.
		TreeMap<Long, Content> contents = new FlogFactory(flog).getContentsTreeMap(true);
		Content[] c = new Content[contents.size()];
		c = contents.values().toArray(c);
		for(int i=c.length-1;i>=0;i--) {
			final Content content = c[i];
			final HTMLNode row = tBody.addChild("tr");

			if(content.isDraft()) {
				row.addAttribute("style", "background-color: yellow;");
			}

			row.addChild("td").addChild("pre", DataFormatter.toString(content.getID()));
			row.addChild("td").addChild("%", DataFormatter.toString(DataFormatter.htmlSpecialChars(content.getTitle())));
			row.addChild("td", DataFormatter.toString(DataFormatter.LocalDateFormatter.format(content.getContentCreationDate())));
			row.addChild("td", DataFormatter.toString(DataFormatter.LocalDateFormatter.format(content.getContentModificationDate())));

			final HTMLNode formDetails = FlogHelper.getPR().addFormChild(row.addChild("td"), FlogHelperToadlet.BASE_URI +
					PreviewToadlet.MY_URI + flog.getID() + "/Content-" + content.getID() + ".html", "ContentDetails-" + content.getID());
			formDetails.addAttribute("method", "get");
			formDetails.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Preview")});

			final HTMLNode formDelete = FlogHelper.getPR().addFormChild(row.addChild("td"), this.path(),
					"DeleteContent-" + content.getID());
			formDelete.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Delete")});
			formDelete.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "ContentToDelete", DataFormatter.toString(content.getID())});
			formDelete.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.getID())});

			final HTMLNode formEdit = FlogHelper.getPR().addFormChild(row.addChild("td"), FlogHelperToadlet.BASE_URI +
					CreateOrEditContentToadlet.MY_URI, "EditContent-" + content.getID());
			formEdit.addAttribute("method", "get");
			formEdit.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Edit")});
			formEdit.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.getID())});
			formEdit.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "ContentID", DataFormatter.toString(content.getID())});
		}

		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}

	public void getPagePost(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final Flog flog = new PluginStoreFlog(this.getParameterWhetherItIsPostOrGet(request, "FlogID", 7));

		final String idToDelete = request.getPartAsString("ContentToDelete", 7);
		final String idToReallyDelete = request.getPartAsString("ContentToReallyDelete", 7);

		if (idToReallyDelete != null && !idToReallyDelete.equals("")) {
			if (request.getPartAsString("Yes", 3).equals("Yes")) {
				flog.deleteContent(idToReallyDelete);
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
					new String[]{"hidden", "FlogID", flog.getID()});
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
