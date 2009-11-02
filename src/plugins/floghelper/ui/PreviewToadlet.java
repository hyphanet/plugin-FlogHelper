/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.ui;

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginStore;
import freenet.support.MultiValueTable;
import freenet.support.api.HTTPRequest;
import java.io.IOException;
import java.net.URI;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.ui.flog.FlogFactory;

/**
 *
 * @author Artefact2
 */
public class PreviewToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/Preview/";
	public static final String VIEW_RAW_DEFAULT_TEMPLATE_URI = "DefaultTemplate/";
	public static final String VIEW_DEFAULT_CSS_URI = "DefaultCSS/";

	public PreviewToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	@Override
	public void getPageGet(PageNode pageNode, URI uri, HTTPRequest request, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		this.getPagePost(pageNode, uri, request, ctx);
	}

	@Override
	public void getPagePost(PageNode pageNode, URI uri, HTTPRequest request, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		if (uri.getPath().equals(FlogHelperToadlet.BASE_URI + PreviewToadlet.MY_URI + VIEW_RAW_DEFAULT_TEMPLATE_URI)) {
			previewTemplate(pageNode, uri, request, ctx);
		} else if (uri.getPath().equals(FlogHelperToadlet.BASE_URI + PreviewToadlet.MY_URI + VIEW_DEFAULT_CSS_URI)) {
			previewCSS(pageNode, uri, request, ctx);
		} else {
			final PluginStore flog = FlogHelper.getStore().subStores.get(this.getParameterWhetherItIsPostOrGet(request, "FlogID", 7));
			if (flog == null) {
				this.sendErrorPage(ctx, 404, "Not found", "Incorrect or missing FlogID.");
			}

			String contentID = this.getParameterWhetherItIsPostOrGet(request, "ContentID", 7);

			writeHTMLReply(ctx, 200, "OK", null, new FlogFactory(flog).getContentPage(contentID));
		}
	}

	public static void previewTemplate(PageNode pageNode, URI uri, HTTPRequest request, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		byte[] data = FlogFactory.getTemplate().getBytes();
		ctx.sendReplyHeaders(200, "OK", new MultiValueTable<String, String>(), "text/plain", data.length);
		ctx.writeData(data);
	}

	public static void previewCSS(PageNode pageNode, URI uri, HTTPRequest request, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		byte[] data = FlogFactory.getCSS().getBytes();
		ctx.sendReplyHeaders(200, "OK", new MultiValueTable<String, String>(), "text/plain", data.length);
		ctx.writeData(data);
	}
}
