/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class CreateOrEditFlogToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/CreateOrEditFlog/";

	public CreateOrEditFlogToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	public void handleMethodGET(URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		this.handleMethodPOST(uri, request, ctx);
	}

	public void handleMethodPOST(URI uri, HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		String flogID = request.getPartAsString("FlogID", 7);
		PageNode pageNode = FlogHelper.getPR().getPageMaker().getPageNode("FlogHelper", ctx);

		if (request.isPartSet("Yes")) {
			PluginStore flog;

			if (FlogHelper.getStore().subStores.containsKey(flogID)) {
				flog = FlogHelper.getStore().subStores.get(flogID);
			} else {
				flog = new PluginStore();
				FlogHelper.getStore().subStores.put(flogID, flog);
			}

			flog.strings.put("ID", flogID);
			flog.strings.put("Title", request.getPartAsString("Title", 100));
			flog.strings.put("DefaultAuthor", request.getPartAsString("DefaultAuthor", 1000));
			flog.strings.put("SmallDescription", request.getPartAsString("SmallDescription", 1000));
			FlogHelper.putStore();

			HTMLNode infobox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("FlogCreationSuccessful"), pageNode.content);
			infobox.addChild("p", FlogHelper.getBaseL10n().getString("FlogCreationSuccessfulLong"));
			HTMLNode links = infobox.addChild("p");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + FlogListToadlet.MY_URI, FlogHelper.getBaseL10n().getString("ReturnToFlogList"));
			links.addChild("br");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + ContentListToadlet.MY_URI + flog.strings.get("ID"), FlogHelper.getBaseL10n().getString("ViewFlogDetails"));
		} else if (request.isPartSet("No")) {
			HTMLNode infobox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("FlogCreationCancelled"), pageNode.content);
			infobox.addChild("p", FlogHelper.getBaseL10n().getString("FlogCreationCancelledLong"));
			HTMLNode links = infobox.addChild("p");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + FlogListToadlet.MY_URI, FlogHelper.getBaseL10n().getString("ReturnToFlogList"));
			links.addChild("br");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + CreateOrEditFlogToadlet.MY_URI, FlogHelper.getBaseL10n().getString("CreateNewFlog"));
		} else {

			String title;
			PluginStore flog;
			if (flogID.equals("") || !FlogHelper.getStore().subStores.containsKey(flogID)) {
				title = "CreateFlog";
				flogID = DataFormatter.createUniqueFlogID();
				(flog = new PluginStore()).strings.put("ID", flogID);
			} else {
				title = "EditFlog";
				flog = FlogHelper.getStore().subStores.get(flogID);
			}

			HTMLNode form = FlogHelper.getPR().addFormChild(this.getPM().getInfobox(null,
					FlogHelper.getBaseL10n().getString(title), pageNode.content), this.path(), "CreateOrEdit-" + flogID);

			form.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", flogID});

			form.addChild("p").addChild("label", "for", "Title", FlogHelper.getBaseL10n().getString("Title")).addChild("input", new String[]{"type", "size", "name", "value"},
					new String[]{"text", "50", "Title", DataFormatter.toString(flog.strings.get("Title"))});
			form.addChild("p").addChild("label", "for", "DefaultAuthor", FlogHelper.getBaseL10n().getString("DefaultAuthor")).addChild("input", new String[]{"type", "size", "name", "value"},
					new String[]{"text", "50", "DefaultAuthor", DataFormatter.toString(flog.strings.get("DefaultAuthor"))});
			form.addChild("p").addChild("label", "for", "SmallDescription", FlogHelper.getBaseL10n().getString("SmallDescription")).addChild("br").addChild("textarea", new String[]{"rows", "cols", "name"},
					new String[]{"12", "80", "SmallDescription"}, DataFormatter.toString(flog.strings.get("SmallDescription")));

			HTMLNode buttons = form.addChild("p");
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "Yes", FlogHelper.getBaseL10n().getString("Proceed")});
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "No", FlogHelper.getBaseL10n().getString("Cancel")});

		}
		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}
}
