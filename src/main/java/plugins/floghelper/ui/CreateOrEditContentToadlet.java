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

import plugins.floghelper.data.DataFormatter;
import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;
import java.util.HashMap;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.contentsyntax.ContentSyntax;
import plugins.floghelper.data.Content;
import plugins.floghelper.data.Flog;
import plugins.floghelper.data.pluginstore.PluginStoreFlog;

/**
 * This toadlet is used for creating/editing contents.
 * 
 * @author Artefact2
 */
public class CreateOrEditContentToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/CreateOrEditContent/";

	public static final int TITLE_MAXLENGTH = 128;
	public static final int TAGS_MAXLENGTH = 256;

	public CreateOrEditContentToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	public void getPageGet(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		this.getPagePost(pageNode, uri, request, ctx);
	}

	public void getPagePost(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final Flog flog = new PluginStoreFlog(this.getParameterWhetherItIsPostOrGet(request, "FlogID", 7));

		String contentID = this.getParameterWhetherItIsPostOrGet(request, "ContentID", 7);
		final boolean advancedMode = ctx.getContainer().isAdvancedModeEnabled();
		final SimpleDateFormat dateFormat = advancedMode ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") : null;

		if (request.isPartSet("Yes")) {
			Content content;

			if(flog.hasContent(contentID)) {
				content = flog.getContentByID(contentID);
			} else {
				// Content doesn't exist yet
				// We create a new one
				content = flog.newContent();
				flog.putContent(content);
			}

			content.setTitle(request.getPartAsString("Title", TITLE_MAXLENGTH));
			content.setContent(request.getPartAsString("Content", Integer.MAX_VALUE));
			content.setContentSyntax(request.getPartAsString("Content_syntaxes", 1000));
			content.setDraft(request.isPartSet("IsDraft"));

			final Vector<String> tags = new Vector<String>();
			for(String tag : request.getPartAsString("Tags", TAGS_MAXLENGTH).split(",")) {
				tags.add(tag.trim());
			}

            String prevTagsCountStr = request.getPartAsStringFailsafe("PrevTagsCount", 3);
            int prevTagsCount = Integer.parseInt(prevTagsCountStr);
            for(int i = 0; i<prevTagsCount; i++) {
                if(request.isPartSet("PrevTags-" + i)) {
                    tags.add(request.getPartAsStringFailsafe("PrevTags-" + i, 255).trim());
                }
            }

			content.setTags(tags);
			
			if(advancedMode) {
				try {
					content.setContentCreationDate(dateFormat.parse(request.getPartAsStringFailsafe("CreationDate", dateFormat.toPattern().length())));
					content.setContentModificationDate(dateFormat.parse(request.getPartAsStringFailsafe("ModificationDate", dateFormat.toPattern().length())));
				} catch (ParseException e) {
					throw new RuntimeException("Invalid date format, use yyyy-MM-dd HH:mm:ss");
				}
			}

			FlogHelper.putStore();

			final HTMLNode infobox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("ContentCreationSuccessful"), pageNode.content);
			infobox.addChild("p", FlogHelper.getBaseL10n().getString("ContentCreationSuccessfulLong"));
			final HTMLNode links = infobox.addChild("p");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + ContentListToadlet.MY_URI + "?FlogID=" + flog.getID(), FlogHelper.getBaseL10n().getString("ReturnToContentList"));
			links.addChild("br");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + PreviewToadlet.MY_URI + flog.getID() + "/Content-" + content.getID() + ".html", FlogHelper.getBaseL10n().getString("PreviewContent"));
		} else if (request.isPartSet("No")) {
			final HTMLNode infobox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("ContentCreationCancelled"), pageNode.content);
			infobox.addChild("p", FlogHelper.getBaseL10n().getString("ContentCreationCancelledLong"));
			final HTMLNode links = infobox.addChild("p");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + ContentListToadlet.MY_URI + "?FlogID=" + flog.getID(), FlogHelper.getBaseL10n().getString("ReturnToContentList"));
			links.addChild("br");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + CreateOrEditContentToadlet.MY_URI + "?FlogID=" + flog.getID(), FlogHelper.getBaseL10n().getString("CreateContent"));
		} else {
			Content content;

			if(flog.hasContent(contentID)) {
				content = flog.getContentByID(contentID);
			} else {
				content = flog.newContent();
				// Don't put it... yet.
			}

			final HTMLNode form = FlogHelper.getPR().addFormChild(pageNode.content, this.path(), "CreateOrEdit-" + content.getID());

			final HTMLNode generalBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("GeneralContentData"), form, "GeneralContentData", true);
			final HTMLNode tagsBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("Tags"), form, "TagsContentData", true);
			final HTMLNode datesBox = advancedMode ? this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("Dates"), form, "DatesContentData", true) : null;
			final HTMLNode settingsBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("Settings"), form, "SettingsContentData", true);
			final HTMLNode submitBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("SaveChanges"), form, "SubmitContentData", true);

			form.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", flog.getID()});
			form.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "ContentID", content.getID()});

			generalBox.addChild("p").addChild("label", "for", "Title", FlogHelper.getBaseL10n().getString("TitleFieldDesc")).addChild("br").addChild("input", new String[]{"type", "size", "name", "value", "maxlength"},
					new String[]{"text", "50", "Title", DataFormatter.toString(content.getTitle()), Integer.toString(TITLE_MAXLENGTH)});

			final HTMLNode authorsBox = new HTMLNode("select", new String[]{"id", "name"}, new String[]{"Author", "Author"});
			for (final String identityID : this.getWoTIdentities().keySet()) {
				final HTMLNode option = authorsBox.addChild("option", "value", identityID, this.getWoTIdentities().get(identityID));
				if (flog.getAuthorID().equals(identityID)) {
					option.addAttribute("selected", "selected");
				}
			}
			authorsBox.addAttribute("disabled", "disabled");

			generalBox.addChild("p").addChild("label", "for", "Author", FlogHelper.getBaseL10n().getString("AuthorFieldDesc")).addChild("br").addChild(authorsBox);

			ContentSyntax.addJavascriptEditbox(generalBox, "Content",
					content.getContentSyntax(), DataFormatter.toString(content.getContent()),
					FlogHelper.getBaseL10n().getString("ContentFieldDesc"));

			final StringBuilder tagz = new StringBuilder();
			final Vector<String> tags = content.getTags();
			for (String tag : tags) {
				if (tagz.length() == 0) {
					tagz.append(tag);
				} else {
					tagz.append(", ").append(tag);
				}
			}

            final Vector<String> prevTags = this.getPrevTags(flog);

			tagsBox.addChild("p").addChild("label", "for", "Tags", FlogHelper.getBaseL10n().getString("TagsFieldDesc")).addChild("br").addChild("input", new String[]{"type", "size", "name", "value", "maxlength"},
					new String[]{"text", "50", "Tags", tagz.toString(), Integer.toString(TAGS_MAXLENGTH)});
			
			/* Dates box */
			if(datesBox != null) {
				datesBox.addChild("p")
				.addChild("label", "for", "CreationDate", FlogHelper.getBaseL10n().getString("CreationDateFieldDesc"))
				.addChild("input", new String[]{"type", "size", "name", "value", "maxlength"},
					new String[]{"text", Integer.toString(dateFormat.toPattern().length() + 2), "CreationDate", 
						dateFormat.format(content.getContentCreationDate()), Integer.toString(dateFormat.toPattern().length())});
				
				datesBox.addChild("p")
				.addChild("label", "for", "ModificationDate", FlogHelper.getBaseL10n().getString("ModificationDateFieldDesc"))
				.addChild("input", new String[]{"type", "size", "name", "value", "maxlength"},
					new String[]{"text", Integer.toString(dateFormat.toPattern().length() + 2), "ModificationDate", 
						dateFormat.format(content.getContentCreationDate()), Integer.toString(dateFormat.toPattern().length())});
			}


            HTMLNode tagsCheckBlock = tagsBox.addChild("p");
            tagsCheckBlock.addChild("label", "for", "PrevTags", FlogHelper.getBaseL10n().getString("PrevTagsFieldDesc")).addChild("br");
            int tmpCount = 0;
            for(String tag : prevTags) {
                boolean doCheck = false;
                if(tags.contains(tag)) {
                    continue;
                }
                tagsCheckBlock.addChild("input", new String[]{"type", "name", "id", "value"},
                        new String[]{"checkbox", "PrevTags-" + tmpCount, tag, tag}).addChild("span", tag);

                tmpCount++;
            }

            String tmpCountStr = ""+tmpCount;
            tagsCheckBlock.addChild("input", new String[]{"type", "name", "id", "value"},
                    new String[]{"hidden", "PrevTagsCount", "PrevTagsCount", tmpCountStr});

			final boolean isDraft = content.isDraft();
			HTMLNode checkBlock = settingsBox.addChild("p");
			if(isDraft) {
				checkBlock.addAttribute("style", "background-color: yellow;");
			}
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

    public Vector<String> getPrevTags(Flog flog) {
        Vector<String> returnTags = new Vector<String>();
        for(Content c2 : flog.getContents()) {
            if(c2.isDraft()) continue;
            for(String tag : c2.getTags()) {
				if(returnTags.contains(tag)) {
                    continue;
				} else {
                    returnTags.add(tag);
				}
            }
        }

        return returnTags;
    }

	/**
	 * A very simple class to represent tags.
	 */
	private class Tag {
		public static final int S_NORMAL = 0;
		public static final int S_MINOR = -1;
		public static final int S_MAJOR = 1;
		/**
		 * The name of the tag.
		 */
		public String name;
		/**
		 * The number of articles having this tag in the flog we are
		 * currently parsing.
		 */
		public long articleCount;
		/**
		 * Status of the tag : minor, normal, major
		 */
		public int status;
	}
}
