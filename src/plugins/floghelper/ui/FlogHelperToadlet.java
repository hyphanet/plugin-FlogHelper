/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.ui;

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageMaker;
import freenet.clients.http.PageNode;
import freenet.clients.http.Toadlet;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.support.api.HTTPRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.fcp.wot.WoTOwnIdentities;

/**
 * Base Toadlet for FlogHelper pages.
 *
 * @author Artefact2
 */
public abstract class FlogHelperToadlet extends Toadlet {

	public static final String BASE_URI = "/floghelper";
	private final String path;
	private Map<String, String> wotIdentities;

	public FlogHelperToadlet(final HighLevelSimpleClient hlsc, final String path) {
		super(hlsc);
		this.path = path;
	}

	@Override
	public String path() {
		return BASE_URI + this.path;
	}

	public String getURIArgument(final HTTPRequest request) {
		return request.getPath().substring(this.path().length()).split("\\?")[0];
	}

	public PageMaker getPM() {
		return FlogHelper.getPR().getPageMaker();
	}

	public Map<String, String> getWoTIdentities() {
		return this.wotIdentities;
	}

	public String getParameterWhetherItIsPostOrGet(HTTPRequest request, String param, int length) {
		if (request.isPartSet(param)) {
			return request.getPartAsString(param, length);
		}

		if (request.isParameterSet(param)) {
			return request.getParam(param);
		}

		return null;
	}

	public boolean makeGlobalChecks(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		// Make sure WoT is there
		try {
			WoTOwnIdentities.sendPing();
		} catch (PluginNotFoundException ex) {
			this.getPM().getInfobox("infobox-error", FlogHelper.getBaseL10n().getString("MissingWoT"),
					pageNode.content).addChild("p", FlogHelper.getBaseL10n().getString("MissingWoTLong"));
			writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
			return false;
		}

		try {
			this.wotIdentities = WoTOwnIdentities.getWoTIdentities();
		} catch (PluginNotFoundException ex) {
			// Safe to ignore
		}

		// Make sure we have at least one identity
		if (this.wotIdentities.size() == 0) {
			this.getPM().getInfobox("infobox-error", FlogHelper.getBaseL10n().getString("MissingWoTIdentity"),
					pageNode.content).addChild("p", FlogHelper.getBaseL10n().getString("MissingWoTIdentityLong"));
			writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
			return false;
		}

		return true;
	}

	public void handleMethodGET(final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final PageNode pageNode = FlogHelper.getPR().getPageMaker().getPageNode(FlogHelper.PLUGIN_NAME, ctx);

		if (!this.makeGlobalChecks(pageNode, uri, request, ctx)) {
			return;
		}

		this.getPageGet(pageNode, uri, request, ctx);
	}

	public void handleMethodPOST(final URI uri, HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final PageNode pageNode = FlogHelper.getPR().getPageMaker().getPageNode(FlogHelper.PLUGIN_NAME, ctx);

		if (!this.makeGlobalChecks(pageNode, uri, request, ctx)) {
			return;
		}

		this.getPagePost(pageNode, uri, request, ctx);
	}

	public abstract void getPageGet(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException;

	public abstract void getPagePost(final PageNode pageNode, final URI uri, HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException;
}
