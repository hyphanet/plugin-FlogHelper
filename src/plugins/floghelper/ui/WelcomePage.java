/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins.floghelper.ui;

import freenet.pluginmanager.PluginStore;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;
import plugins.floghelper.FlogHelper;

/**
 *
 * @author romain
 */
public class WelcomePage extends Page {

	@Override
	public HTMLNode getContent(HTMLNode content, HTTPRequest request) {
		HTMLNode table = content.addChild("table");
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
		headersRow.addChild("th").addChild("form", "action", Page.URI_COMMON + "/CreateNewFlog/")
				.addChild("input", "type", "submit").addAttribute("value", FlogHelper.getBaseL10n().getString("CreateFlog"));

		tHead.addChild(headersRow);
		tFoot.addChild(headersRow);

		for(PluginStore flog : FlogHelper.getStore().subStores.values()) {
			HTMLNode row = tBody.addChild("tr");
			row.addChild("td", DataFormatter.toString(flog.strings.get("ID")));
			row.addChild("td", DataFormatter.toString(flog.strings.get("Activelink")));
			row.addChild("td", DataFormatter.toString(flog.strings.get("Title")));
			row.addChild("td", DataFormatter.toString(flog.strings.get("SmallDescription")));
			row.addChild("td", DataFormatter.toString(flog.subStores.size()));
			row.addChild("td").addChild("form", "action", Page.URI_COMMON + "/FlogDetails/" +
					DataFormatter.toString(flog.strings.get("ID"))).addChild("input", "type", "submit").
					addAttribute("value", FlogHelper.getBaseL10n().getString("Details"));
		}

		content.addChild("pre", DataFormatter.printStore(FlogHelper.getStore()));

		return content;
	}
}
