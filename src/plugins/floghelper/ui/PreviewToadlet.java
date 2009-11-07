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
			final String flogID = uri.getPath().replace(FlogHelperToadlet.BASE_URI + PreviewToadlet.MY_URI, "").split("/")[0];
			final PluginStore flog = FlogHelper.getStore().subStores.get(flogID);
			final FlogFactory factory = new FlogFactory(flog);
			if (flog == null) {
				this.sendErrorPage(ctx, 404, "Not found", "Incorrect or missing FlogID.");
			} else {
				final String file = uri.getPath().replace(FlogHelperToadlet.BASE_URI + PreviewToadlet.MY_URI, "").replace(flogID, "");
				if(file.equals("/") || file.equals("/index.html")) {
					writeHTMLReply(ctx, 200, "OK", null, factory.getIndex());
				} else if(file.startsWith("/Content-") && file.endsWith(".html")) {
					final String contentID = file.replace("/Content-", "").replace(".html", "");
					if(flog.subStores.containsKey(contentID)) {
						writeHTMLReply(ctx, 200, "OK", null, factory.getContentPage(contentID));
					} else {
						this.sendErrorPage(ctx, 404, "Not found", "Incorrect or missing ContentID.");
					}
				} else if(file.startsWith("/Archives-p") && file.endsWith(".html")) {
					writeHTMLReply(ctx, 200, "OK", null, factory.getArchives(Long.parseLong(file.replace("/Archives-p", "").replace(".html", ""))));
				} else if(file.startsWith("/Tag-") && file.endsWith(".html")) {
					final long page = Long.parseLong(file.replaceAll("^/Tag-(.+?)-p([0-9]+)\\.html$", "$2"));
					final String tag = file.replaceAll("^/Tag-(.+?)-p([0-9]+)\\.html$", "$1");
					writeHTMLReply(ctx, 200, "OK", null, factory.getTagsPage(tag, page));
				} else if(file.equals("/GlobalStyle.css")) {
					byte[] data = new FlogFactory(flog).getCSS().getBytes();
					ctx.sendReplyHeaders(200, "OK", new MultiValueTable<String, String>(), "text/css", data.length);
					ctx.writeData(data);
				} else if(file.equals("/AtomFeed.xml")) {
					byte[] data = new FlogFactory(flog).getAtomFeed().getBytes();
					ctx.sendReplyHeaders(200, "OK", new MultiValueTable<String, String>(), "application/atom+xml", data.length);
					ctx.writeData(data);
				} else if(file.equals("/activelink.png")) {
					byte[] data = flog.bytesArrays.get("Activelink");
					if(data != null) {
						ctx.sendReplyHeaders(200, "OK", new MultiValueTable<String, String>(), "image/png", data.length);
						ctx.writeData(data);
					}
				} else {
					this.sendErrorPage(ctx, 404, "Not found", "Unintelligible URI.");
				}
			}
		}
	}

	public static void previewTemplate(PageNode pageNode, URI uri, HTTPRequest request, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		byte[] data = new FlogFactory(new PluginStore()).getTemplate().getBytes();
		ctx.sendReplyHeaders(200, "OK", new MultiValueTable<String, String>(), "text/plain", data.length);
		ctx.writeData(data);
	}

	public static void previewCSS(PageNode pageNode, URI uri, HTTPRequest request, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		byte[] data = new FlogFactory(new PluginStore()).getCSS().getBytes();
		ctx.sendReplyHeaders(200, "OK", new MultiValueTable<String, String>(), "text/plain", data.length);
		ctx.writeData(data);
	}
}
