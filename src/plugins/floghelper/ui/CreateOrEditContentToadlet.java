/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.ui;

import plugins.floghelper.data.DataFormatter;
import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginStore;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Vector;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.contentsyntax.ContentSyntax;
import plugins.floghelper.ui.flog.FlogFactory;

/**
 * This toadlet is used for creating/editing contents.
 * 
 * @author Artefact2
 */
public class CreateOrEditContentToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/CreateOrEditContent/";

	public CreateOrEditContentToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	public void getPageGet(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		this.getPagePost(pageNode, uri, request, ctx);
	}

	public void getPagePost(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final PluginStore flog = FlogHelper.getStore().subStores.get(this.getParameterWhetherItIsPostOrGet(request, "FlogID", 7));
		if (flog == null) {
			this.sendErrorPage(ctx, 404, "Not found", "Incorrect or missing FlogID.");
		}

		String contentID = this.getParameterWhetherItIsPostOrGet(request, "ContentID", 7);

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
			content.strings.put("Content", request.getPartAsString("Content", Integer.MAX_VALUE));
			content.strings.put("ContentSyntax", request.getPartAsString("Content_syntaxes", 1000));
			content.booleans.put("IsDraft", request.isPartSet("IsDraft"));
			if (content.longs.get("CreationDate") == null) {
				content.longs.put("CreationDate", System.currentTimeMillis());
			}
			content.longs.put("LastModification", System.currentTimeMillis());

			final Vector<String> tags = new Vector<String>();
			for(String tag : request.getPartAsString("Tags", 1000).split(",")) {
				tags.add(tag.trim());
			}

			final String[] sTags = new String[tags.size()];
			for(int i = 0; i < sTags.length; ++i) {
				sTags[i] = tags.elementAt(i);
			}
			content.stringsArrays.put("Tags", sTags);

			FlogHelper.putStore();

			final HTMLNode infobox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("ContentCreationSuccessful"), pageNode.content);
			infobox.addChild("p", FlogHelper.getBaseL10n().getString("ContentCreationSuccessfulLong"));
			final HTMLNode links = infobox.addChild("p");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + ContentListToadlet.MY_URI + "?FlogID=" + flog.strings.get("ID"), FlogHelper.getBaseL10n().getString("ReturnToContentList"));
			links.addChild("br");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + PreviewToadlet.MY_URI + flog.strings.get("ID") + "/Content-" + content.strings.get("ID") + ".html", FlogHelper.getBaseL10n().getString("PreviewContent"));
		} else if (request.isPartSet("No")) {
			final HTMLNode infobox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("ContentCreationCancelled"), pageNode.content);
			infobox.addChild("p", FlogHelper.getBaseL10n().getString("ContentCreationCancelledLong"));
			final HTMLNode links = infobox.addChild("p");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + ContentListToadlet.MY_URI + "?FlogID=" + flog.strings.get("ID"), FlogHelper.getBaseL10n().getString("ReturnToContentList"));
			links.addChild("br");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + CreateOrEditContentToadlet.MY_URI + "?FlogID=" + flog.strings.get("ID"), FlogHelper.getBaseL10n().getString("CreateNewContent"));
		} else {
			final String title;
			final PluginStore content;
			if (contentID == null || contentID.equals("") || !flog.subStores.containsKey(contentID)) {
				title = "CreateContent";
				contentID = DataFormatter.createSubStoreUniqueID(flog);
				(content = new PluginStore()).strings.put("ID", contentID);
			} else {
				title = "EditContent";
				content = flog.subStores.get(contentID);
			}

			final HTMLNode form = FlogHelper.getPR().addFormChild(pageNode.content, this.path(), "CreateOrEdit-" + contentID);

			final HTMLNode generalBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("GeneralContentData"), form, "GeneralContentData", true);
			final HTMLNode tagsBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("Tags"), form, "TagsContentData", true);
			final HTMLNode settingsBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("Settings"), form, "SettingsContentData", true);
			final HTMLNode submitBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("SaveChanges"), form, "SubmitContentData", true);

			form.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", flog.strings.get("ID")});
			form.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "ContentID", contentID});

			generalBox.addChild("p").addChild("label", "for", "Title", FlogHelper.getBaseL10n().getString("TitleFieldDesc")).addChild("br").addChild("input", new String[]{"type", "size", "name", "value"},
					new String[]{"text", "50", "Title", DataFormatter.toString(content.strings.get("Title"))});

			final HTMLNode authorsBox = new HTMLNode("select", new String[]{"id", "name"}, new String[]{"Author", "Author"});
			for (final String identityID : this.getWoTIdentities().keySet()) {
				final HTMLNode option = authorsBox.addChild("option", "value", identityID, this.getWoTIdentities().get(identityID));
				if (flog.strings.get("Author").equals(identityID)) {
					option.addAttribute("selected", "selected");
				}
			}
			authorsBox.addAttribute("disabled", "disabled");

			generalBox.addChild("p").addChild("label", "for", "Author", FlogHelper.getBaseL10n().getString("AuthorFieldDesc")).addChild("br").addChild(authorsBox);

			ContentSyntax.addJavascriptEditbox(generalBox, "Content",
					content.strings.get("ContentSyntax"), DataFormatter.toString(content.strings.get("Content")),
					FlogHelper.getBaseL10n().getString("ContentFieldDesc"));

			final StringBuilder tagz = new StringBuilder();
			final String[] tags = content.stringsArrays.get("Tags");
			if(tags != null) {
			for (String tag : tags) {
				if (tagz.length() == 0) {
					tagz.append(tag);
				} else {
					tagz.append(", ").append(tag);
				}
			}
			}
			tagsBox.addChild("p").addChild("label", "for", "Tags", FlogHelper.getBaseL10n().getString("TagsFieldDesc")).addChild("br").addChild("input", new String[]{"type", "size", "name", "value"},
					new String[]{"text", "50", "Tags", tagz.toString()});

			final boolean isDraft = content.booleans.get("IsDraft") == null ? FlogFactory.DEFAULT_DRAFT_STATUS : content.booleans.get("IsDraft");
			HTMLNode checkBlock = settingsBox.addChild("p");
			checkBlock.addChild("input", new String[]{"type", "name", "id", isDraft ? "checked" : "class"},
					new String[]{"checkbox", "IsDraft", "IsDraft", isDraft ? "checked" : ""});
			checkBlock.addChild("label", "for", "IsDraft", FlogHelper.getBaseL10n().getString("IsDraftDesc"));


			final HTMLNode buttons = submitBox.addChild("p");
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "Yes", FlogHelper.getBaseL10n().getString("Proceed")});
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "No", FlogHelper.getBaseL10n().getString("Cancel")});

		}
		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}
}
