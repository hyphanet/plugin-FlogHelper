/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.ui;

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginStore;
import freenet.support.Logger;
import freenet.support.api.HTTPRequest;
import java.io.IOException;
import java.net.URI;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.contentsyntax.ContentSyntax;

/**
 *
 * @author Artefact2
 */
public class PreviewToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/ViewContent/";

	public PreviewToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	@Override
	public void getPageGet(PageNode pageNode, URI uri, HTTPRequest request, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		this.getPagePost(pageNode, uri, request, ctx);
	}

	@Override
	public void getPagePost(PageNode pageNode, URI uri, HTTPRequest request, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final PluginStore flog = FlogHelper.getStore().subStores.get(this.getParameterWhetherItIsPostOrGet(request, "FlogID", 7));
		if (flog == null) {
			this.sendErrorPage(ctx, 404, "Not found", "Incorrect or missing FlogID.");
		}

		String contentID = this.getParameterWhetherItIsPostOrGet(request, "ContentID", 7);
		PluginStore content = flog.subStores.get(contentID);
		assert (content != null);

		StringBuilder generatedPreview = new StringBuilder();

		generatedPreview.append("<html><head><title>Preview</title></head><body>");
		String syntax = content.strings.get("ContentSyntax");
		if (syntax == null) {
			syntax = "RawXHTML";
		}

		try {
			generatedPreview.append(((ContentSyntax) Class.forName("plugins.floghelper.contentsyntax." + syntax).newInstance()).parseSomeString(content.strings.get("Content")));
		} catch (InstantiationException ex) {
			Logger.error(this, "Cannot instanciate Content syntax " + content.strings.get("ContentSyntax"));
		} catch (IllegalAccessException ex) {
			Logger.error(this, "Cannot instanciate Content syntax " + content.strings.get("ContentSyntax"));
		} catch (ClassNotFoundException ex) {
			Logger.error(this, "Cannot instanciate Content syntax " + content.strings.get("ContentSyntax"));
		}

		generatedPreview.append("</body></html>");
		writeHTMLReply(ctx, 200, "OK", null, generatedPreview.toString());
	}
}
