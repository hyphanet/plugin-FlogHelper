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
import freenet.keys.USK;
import freenet.l10n.BaseL10n.LANGUAGE;
import freenet.support.HTMLNode;
import freenet.support.Logger;
import freenet.support.api.HTTPRequest;
import freenet.support.api.HTTPUploadedFile;
import freenet.support.io.Closer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import javax.imageio.ImageIO;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.contentsyntax.ContentSyntax;
import plugins.floghelper.data.Activelink;
import plugins.floghelper.data.Flog;
import plugins.floghelper.data.pluginstore.PluginStoreFlog;
import plugins.floghelper.ui.flog.FlogFactory;

/**
 * This toadlet is used for editing/creating flogs.
 *
 * @author Artefact2
 */
public class CreateOrEditFlogToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/CreateOrEditFlog/";

	public static final int TITLE_MAXLENGTH = 128;
	public static final int DESCRIPTION_MAXLENGTH = 128;
	public static final int SSKPATH_MAXLENGTH = 128;
	public static final int AUTHOR_MAXLENGTH = 1024;

	public CreateOrEditFlogToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	public void getPageGet(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		this.getPagePost(pageNode, uri, request, ctx);
	}

	public void getPagePost(final PageNode pageNode, final URI uri, HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		String flogID = this.getParameterWhetherItIsPostOrGet(request, "FlogID", 7);

		if (request.isPartSet("Yes")) {
			Flog flog;

			if(PluginStoreFlog.hasFlog(flogID)) {
				flog = new PluginStoreFlog(flogID);
			} else {
				flog = new PluginStoreFlog();
				flog.putFlog();
			}

			// Unsubscribe first if we change the author or the SSK path
			try {
				FlogHelper.getUSKManager().unsubscribe(USK.create(flog.getRequestURI()), flog.getUSKCallback());
			} catch (Exception ex) {
				Logger.error(this, "", ex);
			}

			flog.setTitle(request.getPartAsString("Title", TITLE_MAXLENGTH));
			flog.setTheme(request.getPartAsString("Theme", 250));
			flog.setLang(LANGUAGE.mapToLanguage(request.getPartAsString("Lang", 5)));
			flog.setShortDescription(request.getPartAsString("SmallDescription", DESCRIPTION_MAXLENGTH));
			flog.shouldPublishDates(request.isPartSet("PublishContentModificationDate"));
			flog.shouldPublishLibraryIndex(request.isPartSet("InsertLibraryIndex"));
			flog.shouldSortTagsByCount(request.isPartSet("SortTagsByCount"));
			flog.overrideTemplate(request.isPartSet("OverrideTemplate"));
			flog.setTemplateOverride(request.getPartAsString("OverrideTemplateValue", Integer.MAX_VALUE));
			flog.overrideCSS(request.isPartSet("OverrideCSS"));
			flog.setCSSOverride(request.getPartAsString("OverrideCSSValue", Integer.MAX_VALUE));
			flog.setNumberOfContentsOnIndex(DataFormatter.tryParseLong(request.getPartAsString("NumberOfContentsOnIndex", 10), flog.getNumberOfContentsOnIndex()));
			flog.setNumberOfContentsOnArchives(DataFormatter.tryParseLong(request.getPartAsString("NumberOfContentsOnArchives", 10), flog.getNumberOfContentsOnArchives()));

			final String sskPath = request.getPartAsString("SSKPath", SSKPATH_MAXLENGTH).trim();
			final String authorID = request.getPartAsString("Author", AUTHOR_MAXLENGTH);

			if(!sskPath.equals(flog.getSSKPath()) || !authorID.equals(flog.getAuthorID())) {
				// We changed the flog access URI : the latest USK edition is no longer valid.
				flog.setLatestUSKEdition(0L);
			}

			flog.setAuthorID(authorID);

			// A SSK path should not be just a number, should obviously not be empty
			// and shouldn't contain any "/". It also mustn't be equal to "WoT" as
			// this will screw the identity...
			if(sskPath.length() > 0 && !sskPath.matches("^[0-9]+$") && !sskPath.contains("/") && !sskPath.equals("WoT")) {
				flog.setSSKPath(sskPath);
			} else {
				final HTMLNode infobox = this.getPM().getInfobox("infobox-error", FlogHelper.getBaseL10n().getString("Error"), pageNode.content);
				infobox.addChild("p", FlogHelper.getBaseL10n().getString("InvalidSSKPath"));
			}

			if (request.isPartSet("ActivelinkDelete")) {
				flog.destroyActivelink();
			} else if (request.isPartSet("Activelink")) {
				HTTPUploadedFile activelink = request.getUploadedFile("Activelink");
				if (activelink.getContentType() != null) {
					if (activelink.getContentType().toLowerCase().equals(Activelink.MIMETYPE)) {
						BufferedImage imageActivelink = ImageIO.read(activelink.getData().getInputStream());
						if (imageActivelink.getHeight() == Activelink.HEIGHT && imageActivelink.getWidth() == Activelink.WIDTH) {
							flog.setActivelink(Activelink.getByteArrayFromUploadedFile(activelink));
							Closer.close(activelink.getData());
						} else {
							final HTMLNode infobox = this.getPM().getInfobox("infobox-error", FlogHelper.getBaseL10n().getString("ActivelinkError"), pageNode.content);
							infobox.addChild("p", FlogHelper.getBaseL10n().getString("ActivelinkInvalidDimensions"));
							final HTMLNode links = infobox.addChild("p");
							links.addChild("a", "href", FlogHelperToadlet.BASE_URI + CreateOrEditFlogToadlet.MY_URI + "?FlogID=" + flogID, FlogHelper.getBaseL10n().getString("ReturnToFlogEdit"));
						}
					} else if(!activelink.getFilename().equals("")) {
						final HTMLNode infobox = this.getPM().getInfobox("infobox-error", FlogHelper.getBaseL10n().getString("ActivelinkError"), pageNode.content);
						infobox.addChild("p", FlogHelper.getBaseL10n().getString("ActivelinkMustBeAPNGPicture"));
						final HTMLNode links = infobox.addChild("p");
						links.addChild("a", "href", FlogHelperToadlet.BASE_URI + CreateOrEditFlogToadlet.MY_URI + "?FlogID=" + flogID, FlogHelper.getBaseL10n().getString("ReturnToFlogEdit"));
					}
				}
			}

			FlogHelper.putStore();
			FlogHelper.subscribeToFlogUSKs();

			final HTMLNode infobox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("FlogCreationSuccessful"), pageNode.content);
			infobox.addChild("p", FlogHelper.getBaseL10n().getString("FlogCreationSuccessfulLong"));
			final HTMLNode links = infobox.addChild("p");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + FlogListToadlet.MY_URI, FlogHelper.getBaseL10n().getString("ReturnToFlogList"));
			links.addChild("br");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + ContentListToadlet.MY_URI + "?FlogID=" + flog.getID(), FlogHelper.getBaseL10n().getString("ViewFlogDetails"));
		} else if (request.isPartSet("No")) {
			final HTMLNode infobox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("FlogCreationCancelled"), pageNode.content);
			infobox.addChild("p", FlogHelper.getBaseL10n().getString("FlogCreationCancelledLong"));
			final HTMLNode links = infobox.addChild("p");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + FlogListToadlet.MY_URI, FlogHelper.getBaseL10n().getString("ReturnToFlogList"));
			links.addChild("br");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + CreateOrEditFlogToadlet.MY_URI, FlogHelper.getBaseL10n().getString("CreateFlog"));
		} else {
			Flog flog;

			if(PluginStoreFlog.hasFlog(flogID)) {
				flog = new PluginStoreFlog(flogID);
			} else {
				flog = new PluginStoreFlog();
			}

			final HTMLNode form = FlogHelper.getPR().addFormChild(pageNode.content, this.path(), "CreateOrEdit-" + flog.getID());

			final HTMLNode generalBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("GeneralFlogData"), form, "GeneralFlogData", true);
			final HTMLNode activelinkBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("Activelink"), form, "ActivelinkFlogData", true);
			final HTMLNode settingsBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("FlogSettings"), form, "SettingsFlogData", true);
			final HTMLNode submitBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("SaveChanges"), form, "SubmitFlogData", true);

			form.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", flog.getID()});

			generalBox.addChild("p").addChild("label", "for", "Title", FlogHelper.getBaseL10n().getString("TitleFieldDesc")).addChild("br").addChild("input", new String[]{"type", "size", "name", "value", "maxlength"},
					new String[]{"text", "50", "Title", DataFormatter.toString(flog.getTitle()), Integer.toString(TITLE_MAXLENGTH)});

			final HTMLNode authorsBox = new HTMLNode("select", new String[]{"id", "name"}, new String[]{"Author", "Author"});
			for (final String identityID : this.getWoTIdentities().keySet()) {
				final HTMLNode option = authorsBox.addChild("option", "value", identityID, this.getWoTIdentities().get(identityID));
				if (flog.getAuthorID().equals(identityID)) {
					option.addAttribute("selected", "selected");
				}
			}

			generalBox.addChild("p").addChild("label", "for", "Author", FlogHelper.getBaseL10n().getString("AuthorFieldDesc")).addChild("br").addChild(authorsBox);

			final HTMLNode langBox = new HTMLNode("select", new String[]{"id", "name"}, new String[]{"Lang", "Lang"});
			for (final LANGUAGE l : LANGUAGE.values()) {
				final HTMLNode option = langBox.addChild("option", "value", l.shortCode, l.fullName);
				if (flog.getLang().shortCode.equals(l.shortCode)) {
					option.addAttribute("selected", "selected");
				}
			}

			generalBox.addChild("p").addChild("label", "for", "Lang", FlogHelper.getBaseL10n().getString("LangFieldDesc")).addChild("br").addChild(langBox);


			// Most browsers probably won't care about the accept="image/png" attribute
			// But we put it anyway... because it's semantic
			activelinkBox.addChild("p").addChild("label", "for", "Activelink", FlogHelper.getBaseL10n().getString("ActivelinkFieldDesc").replace("${Width}", Integer.toString(Activelink.WIDTH)).replace("${Height}", Integer.toString(Activelink.HEIGHT))).addChild("br").addChild("input", new String[]{"type", "accept", "name"},
					new String[]{"file", Activelink.MIMETYPE, "Activelink"});
			HTMLNode checkBlock = activelinkBox.addChild("p");
			checkBlock.addChild("input", new String[]{"type", "name", "id"},
					new String[]{"checkbox", "ActivelinkDelete", "ActivelinkDelete"});
			checkBlock.addChild("label", "for", "ActivelinkDelete", FlogHelper.getBaseL10n().getString("ActivelinkDeleteFieldDesc"));

			generalBox.addChild("p").addChild("label", "for", "SmallDescription", FlogHelper.getBaseL10n().getString("SmallDescriptionFieldDesc")).addChild("br")
					.addChild("input", new String[]{"type", "size", "name", "value", "maxlength"},
					new String[]{"text", "50", "SmallDescription", flog.getShortDescription(), Integer.toString(DESCRIPTION_MAXLENGTH)});

			final boolean insertLibraryIndex = flog.shouldPublishLibraryIndex();
			checkBlock = settingsBox.addChild("p");
			checkBlock.addChild("input", new String[]{"type", "name", "id", insertLibraryIndex ? "checked" : "class"},
					new String[]{"checkbox", "InsertLibraryIndex", "InsertLibraryIndex", insertLibraryIndex ? "checked" : ""});
			checkBlock.addChild("label", "for", "InsertLibraryIndex", FlogHelper.getBaseL10n().getString("InsertLibraryIndexDesc"));

			final boolean publishContentModificationDate = flog.shouldPublishDates();
			checkBlock = settingsBox.addChild("p");
			checkBlock.addChild("input", new String[]{"type", "name", "id", publishContentModificationDate ? "checked" : "class"},
					new String[]{"checkbox", "PublishContentModificationDate", "PublishContentModificationDate", publishContentModificationDate ? "checked" : ""});
			checkBlock.addChild("label", "for", "PublishContentModificationDate", FlogHelper.getBaseL10n().getString("PublishContentModificationDateDesc"));

			final boolean sortTagsByCount = flog.shouldSortTagsByCount();
			checkBlock = settingsBox.addChild("p");
			checkBlock.addChild("input", new String[]{"type", "name", "id", sortTagsByCount ? "checked" : "class"},
					new String[]{"checkbox", "SortTagsByCount", "SortTagsByCount", sortTagsByCount ? "checked" : ""});
			checkBlock.addChild("label", "for", "SortTagsByCount", FlogHelper.getBaseL10n().getString("SortTagsByCountDesc"));

			settingsBox.addChild("p").addChild("label", "for", "NumberOfContentsOnIndex", FlogHelper.getBaseL10n().getString("NumberOfContentsOnIndexFieldDesc")).addChild("br")
					.addChild("input", new String[]{"type", "size", "name", "value"},
					new String[]{"text", "4", "NumberOfContentsOnIndex", Long.toString(flog.getNumberOfContentsOnIndex())});
			settingsBox.addChild("p").addChild("label", "for", "NumberOfContentsOnArchives", FlogHelper.getBaseL10n().getString("NumberOfContentsOnArchivesFieldDesc")).addChild("br")
					.addChild("input", new String[]{"type", "size", "name", "value"},
					new String[]{"text", "4", "NumberOfContentsOnArchives", Long.toString(flog.getNumberOfContentsOnArchives())});
			settingsBox.addChild("p").addChild("label", "for", "SSKPath", FlogHelper.getBaseL10n().getString("SSKPathDesc")).addChild("br")
					.addChild("input", new String[]{"type", "size", "name", "value", "maxlength"},
					new String[]{"text", "20", "SSKPath", flog.getSSKPath(), Integer.toString(SSKPATH_MAXLENGTH)});

			HTMLNode themesBox = settingsBox.addChild("p");
			themesBox.addChild("label", "for", "Theme", FlogHelper.getBaseL10n().getString("ThemeFieldDesc"));

			final HTMLNode themeSelection = themesBox.addChild("br").addChild("select", new String[]{"id", "name"}, new String[]{"Theme", "Theme"});
			for (final String theme : FlogFactory.THEMES) {
				final HTMLNode option = themeSelection.addChild("option", "value", theme, theme);
				if (flog.getTheme().equals(theme)) {
					option.addAttribute("selected", "selected");
				}
			}

			if(ctx.getContainer().isAdvancedModeEnabled()) {
			themesBox.addChild("a", "href", FlogHelperToadlet.BASE_URI + PreviewToadlet.MY_URI + flog.getID() + "/" + PreviewToadlet.VIEW_DEFAULT_CSS_URI,
			        FlogHelper.getBaseL10n().getString("SeeTheDefaultCSS"));
			final HTMLNode templatesBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("Templates"), form, "TemplatesFlogData", true);

			final boolean overrideTemplate = flog.overrideTemplate();
			checkBlock = templatesBox.addChild("p");
			checkBlock.addChild("input", new String[]{"type", "name", "id", overrideTemplate ? "checked" : "class"},
					new String[]{"checkbox", "OverrideTemplate", "OverrideTemplate", overrideTemplate ? "checked" : ""});
			checkBlock.addChild("label", "for", "OverrideTemplate", FlogHelper.getBaseL10n().getString("OverrideTemplateLong"));
			checkBlock.addChild("br");
			checkBlock.addChild("textarea", new String[]{"rows", "cols", "name", "id"},
					new String[]{"12", "80", "OverrideTemplateValue", "OverrideTemplateValue"}, flog.getTemplateOverride());
			checkBlock.addChild("br");
			checkBlock.addChild("a", "href", FlogHelperToadlet.BASE_URI + PreviewToadlet.MY_URI + flog.getID() + "/" + PreviewToadlet.VIEW_RAW_DEFAULT_TEMPLATE_URI,
					FlogHelper.getBaseL10n().getString("SeeTheRawDefaultTemplate"));

			final boolean overrideCSS = flog.overrideCSS();
			checkBlock = templatesBox.addChild("p");
			checkBlock.addChild("input", new String[]{"type", "name", "id", overrideCSS ? "checked" : "class"},
					new String[]{"checkbox", "OverrideCSS", "OverrideCSS", overrideCSS ? "checked" : ""});
			checkBlock.addChild("label", "for", "OverrideCSS", FlogHelper.getBaseL10n().getString("OverrideCSSLong"));
			checkBlock.addChild("br");
			checkBlock.addChild("textarea", new String[]{"rows", "cols", "name", "id"},
					new String[]{"12", "80", "OverrideCSSValue", "OverrideCSSValue"}, flog.getCSSOverride());
			}


			final HTMLNode buttons = submitBox.addChild("p");
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "Yes", FlogHelper.getBaseL10n().getString("Proceed")});
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "No", FlogHelper.getBaseL10n().getString("Cancel")});

		}
		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}
}
