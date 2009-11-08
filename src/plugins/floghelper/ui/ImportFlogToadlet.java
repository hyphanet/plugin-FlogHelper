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
import freenet.support.api.HTTPUploadedFile;
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
			byte[] buf = new byte[(int) i.getData().size()];
			i.getData().getInputStream().read(buf, 0, buf.length);
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
