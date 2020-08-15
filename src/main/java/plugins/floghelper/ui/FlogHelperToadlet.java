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

import freenet.client.HighLevelSimpleClient;
import freenet.client.async.PersistenceDisabledException;
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
		if (this.wotIdentities.isEmpty()) {
			this.getPM().getInfobox("infobox-error", FlogHelper.getBaseL10n().getString("MissingWoTIdentity"),
					pageNode.content).addChild("p", FlogHelper.getBaseL10n().getString("MissingWoTIdentityLong"));
			writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
			return false;
		}

		return true;
	}

	public void handleMethodGET(final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		this.getPM().parseMode(request, ctx.getContainer());
		final PageNode pageNode = FlogHelper.getPR().getPageMaker().getPageNode(FlogHelper.getName(), ctx);

		if (!this.makeGlobalChecks(pageNode, uri, request, ctx)) {
			return;
		}

		this.getPageGet(pageNode, uri, request, ctx);
	}

	public void handleMethodPOST(final URI uri, HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException, PersistenceDisabledException {
		this.getPM().parseMode(request, ctx.getContainer());
		final PageNode pageNode = FlogHelper.getPR().getPageMaker().getPageNode(FlogHelper.getName(), ctx);

		if (!this.makeGlobalChecks(pageNode, uri, request, ctx)) {
			return;
		}

		this.getPagePost(pageNode, uri, request, ctx);
	}

	public abstract void getPageGet(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException;

	public abstract void getPagePost(final PageNode pageNode, final URI uri, HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException, PersistenceDisabledException;
}
