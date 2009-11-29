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
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginStore;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;
import freenet.support.api.HTTPUploadedFile;
import freenet.support.io.BucketTools;
import java.io.IOException;
import java.net.URI;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.data.DataFormatter;

/**
 * This toadlet handles the flog imports.
 *
 * @author Artefact2
 */
public class ImportFlogToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/Import/";

	public ImportFlogToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	public void getPageGet(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		this.getPagePost(pageNode, uri, request, ctx);
	}

	public void getPagePost(final PageNode pageNode, final URI uri, HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		if (request.isPartSet("Import")) {
			HTTPUploadedFile i = request.getUploadedFile("ImportDb");
			byte[] buf = BucketTools.toByteArray(i.getData());
			PluginStore importedFlog = PluginStore.importStore(buf);
			String newID = DataFormatter.createUniqueFlogID();
			importedFlog.strings.put("ID", newID);
			FlogHelper.getStore().subStores.put(newID, importedFlog);
			final HTMLNode infobox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("FlogImportSuccessful"), pageNode.content);
			infobox.addChild("p", FlogHelper.getBaseL10n().getString("FlogImportSuccessfulLong"));
			final HTMLNode links = infobox.addChild("p");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + FlogListToadlet.MY_URI, FlogHelper.getBaseL10n().getString("ReturnToFlogList"));
			links.addChild("br");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + ContentListToadlet.MY_URI + "?FlogID=" + newID, FlogHelper.getBaseL10n().getString("ViewFlogDetails"));
		} else {
			HTMLNode caveat = this.getPM().getInfobox("infobox-warning", FlogHelper.getBaseL10n().getString("Warning"), pageNode.content);
			caveat.addChild("p", FlogHelper.getBaseL10n().getString("ImportFlogWarning"));
			HTMLNode importBox = this.getPM().getInfobox("infobox-minor", FlogHelper.getBaseL10n().getString("ImportFlog"), pageNode.content);
			HTMLNode ulForm = ctx.addFormChild(importBox, this.path(), "ImportFlog").addChild("p");
			ulForm.addChild("label", "for", "ImportDb", FlogHelper.getBaseL10n().getString("ImportFlogDesc")).addChild("br").addChild("input", new String[]{"type", "name"},
					new String[]{"file", "ImportDb"});
			ulForm.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "Import", FlogHelper.getBaseL10n().getString("Proceed")});
		}

		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}
}
