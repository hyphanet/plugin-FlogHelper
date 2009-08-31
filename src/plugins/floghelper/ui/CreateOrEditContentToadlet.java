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
import java.io.IOException;
import java.net.URI;
import plugins.floghelper.FlogHelper;

/**
 *
 * @author Artefact2
 */
public class CreateOrEditContentToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/CreateOrEditContent/";

	public CreateOrEditContentToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	public void getPageGet(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		this.handleMethodPOST(uri, request, ctx);
	}

	public void getPagePost(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final PluginStore flog = this.getFlogID(request);
		if (flog == null) {
			this.sendErrorPage(ctx, 404, "Not found", "Incorrect or missing FlogID.");
		}

		String contentID = request.getPartAsString("ContentID", 7);

		if (request.isPartSet("Yes")) {
			final PluginStore content;

			if (flog.subStores.containsKey(contentID)) {
				content = flog.subStores.get(contentID);
			} else {
				content = new PluginStore();
				flog.subStores.put(contentID, content);
			}

			content.strings.put("ID", contentID);
			content.strings.put("Title", request.getPartAsString("Title", 100));
			content.strings.put("Author", request.getPartAsString("Author", 1000));
			content.strings.put("Content", request.getPartAsString("Content", Integer.MAX_VALUE));
			if (content.longs.get("CreationDate") == null) {
				content.longs.put("CreationDate", System.currentTimeMillis());
			}
			content.longs.put("LastModification", System.currentTimeMillis());
			FlogHelper.putStore();

			final HTMLNode infobox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("ContentCreationSuccessful"), pageNode.content);
			infobox.addChild("p", FlogHelper.getBaseL10n().getString("ContentCreationSuccessfulLong"));
			final HTMLNode links = infobox.addChild("p");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + ContentListToadlet.MY_URI + flog.strings.get("ID"), FlogHelper.getBaseL10n().getString("ReturnToContentList"));
			links.addChild("br");
			// FIXME do not use hardcoded uri here
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + "/ViewContent/" + content.strings.get("ID"), FlogHelper.getBaseL10n().getString("PreviewContent"));
		} else if (request.isPartSet("No")) {
			final HTMLNode infobox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("ContentCreationCancelled"), pageNode.content);
			infobox.addChild("p", FlogHelper.getBaseL10n().getString("ContentCreationCancelledLong"));
			final HTMLNode links = infobox.addChild("p");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + ContentListToadlet.MY_URI + flog.strings.get("ID"), FlogHelper.getBaseL10n().getString("ReturnToContentList"));
			links.addChild("br");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + CreateOrEditContentToadlet.MY_URI + flog.strings.get("ID"), FlogHelper.getBaseL10n().getString("CreateNewContent"));
		} else {
			final String title;
			final PluginStore content;
			if (contentID.equals("") || !flog.subStores.containsKey(contentID)) {
				title = "CreateContent";
				contentID = DataFormatter.createSubStoreUniqueID(flog);
				(content = new PluginStore()).strings.put("ID", contentID);
				content.strings.put("Author", flog.strings.get("DefaultAuthor"));
			} else {
				title = "EditContent";
				content = flog.subStores.get(contentID);
			}

			final HTMLNode form = FlogHelper.getPR().addFormChild(this.getPM().getInfobox(null,
					FlogHelper.getBaseL10n().getString(title), pageNode.content), this.path(), "CreateOrEdit-" + contentID);

			form.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", flog.strings.get("ID")});
			form.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "ContentID", contentID});

			form.addChild("p").addChild("label", "for", "Title", FlogHelper.getBaseL10n().getString("Title")).addChild("input", new String[]{"type", "size", "name", "value"},
					new String[]{"text", "50", "Title", DataFormatter.toString(content.strings.get("Title"))});

			final HTMLNode authorsBox = new HTMLNode("select", new String[]{"id", "name"}, new String[]{"Author", "Author"});
			for (final String identityID : this.getWoTIdentities().keySet()) {
				final HTMLNode option = authorsBox.addChild("option", "value", identityID, this.getWoTIdentities().get(identityID));
				if (content.strings.get("Author").equals(identityID)) {
					option.addAttribute("selected", "selected");
				}
			}

			form.addChild("p").addChild("label", "for", "Author", FlogHelper.getBaseL10n().getString("AuthorOrEmptyIfAnonymous")).addChild(authorsBox);

			form.addChild("p").addChild("label", "for", "Content", FlogHelper.getBaseL10n().getString("Content")).addChild("br").addChild("textarea", new String[]{"rows", "cols", "name"},
					new String[]{"12", "80", "Content"}, DataFormatter.toString(content.strings.get("Content")));

			final HTMLNode buttons = form.addChild("p");
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "Yes", FlogHelper.getBaseL10n().getString("Proceed")});
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "No", FlogHelper.getBaseL10n().getString("Cancel")});

		}
		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}
}
