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

	public FlogListToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, "/");
	}

	public void handleMethodGET(URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		PageNode pageNode = FlogHelper.getPR().getPageMaker().getPageNode("FlogHelper", ctx);

		HTMLNode table = pageNode.content.addChild("table");
		HTMLNode tHead = table.addChild("thead");
		HTMLNode tFoot = table.addChild("tfoot");
		HTMLNode tBody = table.addChild("tbody");

		HTMLNode headersRow = new HTMLNode("tr");
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("ID"));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("Activelink"));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("Title"));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("SmallDescription"));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("NumberOfEntries"));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("Actions"));
		headersRow.addChild("th").addChild("form", "action", FlogHelperToadlet.BASE_URI +
				"/CreateNewFlog/").addChild("input", "type", "submit").addAttribute("value", FlogHelper.getBaseL10n().getString("CreateFlog"));

		tHead.addChild(headersRow);
		tFoot.addChild(headersRow);

		for (PluginStore flog : FlogHelper.getStore().subStores.values()) {
			HTMLNode row = tBody.addChild("tr");
			row.addChild("td", DataFormatter.toString(flog.strings.get("ID")));
			row.addChild("td", DataFormatter.toString(flog.strings.get("Activelink")));
			row.addChild("td", DataFormatter.toString(flog.strings.get("Title")));
			row.addChild("td", DataFormatter.toString(flog.strings.get("SmallDescription")));
			row.addChild("td", DataFormatter.toString(flog.subStores.size()));
			row.addChild("td").addChild("form", "action", FlogHelperToadlet.BASE_URI +
					"/FlogDetails/" + DataFormatter.toString(flog.strings.get("ID"))).addChild("input", "type", "submit").addAttribute("value",
					FlogHelper.getBaseL10n().getString("Details"));
		}

		pageNode.content.addChild("pre", DataFormatter.printStore(FlogHelper.getStore()));

		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}

	public void handleMethodPOST(URI uri, HTTPRequest request, final ToadletContext ctx) {
	}
}
