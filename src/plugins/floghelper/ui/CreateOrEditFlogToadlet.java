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

	public CreateOrEditFlogToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, "/CreateOrEditFlog/");
	}

	public void handleMethodGET(URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		String flogID = this.getURIArgument(request);
		String title;
		if (flogID.equals("") || FlogHelper.getStore().subStores.get(flogID) == null) {
			title = "CreateFlog";
			flogID = DataFormatter.createFlogID();
		} else {
			title = "EditFlog";
		}

		PageNode pageNode = FlogHelper.getPR().getPageMaker().getPageNode(FlogHelper.getBaseL10n().getString(title), ctx);



		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}

	public void handleMethodPOST(URI uri, HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		PageNode pageNode = FlogHelper.getPR().getPageMaker().getPageNode("EditFlog", ctx);



		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}
}
