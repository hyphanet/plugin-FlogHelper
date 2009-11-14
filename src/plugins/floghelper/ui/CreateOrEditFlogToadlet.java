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
import freenet.support.api.HTTPUploadedFile;
import freenet.support.io.Closer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import javax.imageio.ImageIO;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.contentsyntax.ContentSyntax;
import plugins.floghelper.data.Activelink;
import plugins.floghelper.data.pluginstore.PluginStoreFlog;
import plugins.floghelper.ui.flog.FlogFactory;

/**
 * This toadlet is used for editing/creating flogs.
 *
 * @author Artefact2
 */
public class CreateOrEditFlogToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/CreateOrEditFlog/";

	public CreateOrEditFlogToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	public void getPageGet(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		this.getPagePost(pageNode, uri, request, ctx);
	}

	public void getPagePost(final PageNode pageNode, final URI uri, HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		String flogID = this.getParameterWhetherItIsPostOrGet(request, "FlogID", 7);

		if (request.isPartSet("Yes")) {
			PluginStoreFlog flog;

			try {
				flog = new PluginStoreFlog(flogID);
			} catch (NullPointerException e) {
				flog = new PluginStoreFlog();
				FlogHelper.getStore().subStores.put(flog.getID(), flog.getStore());
			}

			flog.setTitle(request.getPartAsString("Title", 100));
			flog.setAuthorID(request.getPartAsString("Author", 1000));
			flog.setTheme(request.getPartAsString("Theme", 250));
			flog.setShortDescription(request.getPartAsString("SmallDescription", 100000));
			flog.setShortDescriptionSyntax(request.getPartAsString("SmallDescription_syntaxes", 1000));
			flog.shouldPublishStoreDump(request.isPartSet("InsertPluginStoreDump"));
			flog.shouldPublishDates(request.isPartSet("PublishContentModificationDate"));
			flog.shouldPublishLibraryIndex(request.isPartSet("InsertLibraryIndex"));
			flog.overrideTemplate(request.isPartSet("OverrideTemplate"));
			flog.setTemplateOverride(request.getPartAsString("OverrideTemplateValue", 100000));
			flog.overrideCSS(request.isPartSet("OverrideCSS"));
			flog.setCSSOverride(request.getPartAsString("OverrideCSSValue", 100000));
			flog.setNumberOfContentsOnIndex(DataFormatter.tryParseLong(request.getPartAsString("NumberOfContentsOnIndex", 10), flog.getNumberOfContentsOnIndex()));
			flog.setNumberOfContentsOnArchives(DataFormatter.tryParseLong(request.getPartAsString("NumberOfContentsOnArchives", 10), flog.getNumberOfContentsOnArchives()));

			final String sskPath = request.getPartAsString("SSKPath", 30).trim();
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
					} else {
						// FIXME this code is triggered when we DON'T upload a picture too
						//final HTMLNode infobox = this.getPM().getInfobox("infobox-error", FlogHelper.getBaseL10n().getString("ActivelinkError"), pageNode.content);
						//infobox.addChild("p", FlogHelper.getBaseL10n().getString("ActivelinkMustBeAPNGPicture"));
						//final HTMLNode links = infobox.addChild("p");
						//links.addChild("a", "href", FlogHelperToadlet.BASE_URI + CreateOrEditFlogToadlet.MY_URI + "?FlogID=" + flogID, FlogHelper.getBaseL10n().getString("ReturnToFlogEdit"));
					}
				}
			}

			FlogHelper.putStore();

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
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + CreateOrEditFlogToadlet.MY_URI, FlogHelper.getBaseL10n().getString("CreateNewFlog"));
		} else {
			PluginStoreFlog flog;

			try {
				flog = new PluginStoreFlog(flogID);
			} catch (NullPointerException e) {
				flog = new PluginStoreFlog();
			}

			final HTMLNode form = FlogHelper.getPR().addFormChild(pageNode.content, this.path(), "CreateOrEdit-" + flog.getID());

			final HTMLNode generalBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("GeneralFlogData"), form, "GeneralFlogData", true);
			final HTMLNode activelinkBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("Activelink"), form, "ActivelinkFlogData", true);
			final HTMLNode settingsBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("FlogSettings"), form, "SettingsFlogData", true);
			final HTMLNode templatesBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("Templates"), form, "TemplatesFlogData", true);
			final HTMLNode submitBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("SaveChanges"), form, "SubmitFlogData", true);

			form.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", flog.getID()});

			generalBox.addChild("p").addChild("label", "for", "Title", FlogHelper.getBaseL10n().getString("TitleFieldDesc")).addChild("br").addChild("input", new String[]{"type", "size", "name", "value"},
					new String[]{"text", "50", "Title", DataFormatter.toString(flog.getTitle())});

			final HTMLNode authorsBox = new HTMLNode("select", new String[]{"id", "name"}, new String[]{"Author", "Author"});
			for (final String identityID : this.getWoTIdentities().keySet()) {
				final HTMLNode option = authorsBox.addChild("option", "value", identityID, this.getWoTIdentities().get(identityID));
				if (flog.getAuthorID().equals(identityID)) {
					option.addAttribute("selected", "selected");
				}
			}

			generalBox.addChild("p").addChild("label", "for", "Author", FlogHelper.getBaseL10n().getString("AuthorFieldDesc")).addChild("br").addChild(authorsBox);

			// Most browsers probably won't care about the accept="image/png" attribute
			// But we put it anyway... because it's semantic
			activelinkBox.addChild("p").addChild("label", "for", "Activelink", FlogHelper.getBaseL10n().getString("ActivelinkFieldDesc").replace("${Width}", Integer.toString(Activelink.WIDTH)).replace("${Height}", Integer.toString(Activelink.HEIGHT))).addChild("br").addChild("input", new String[]{"type", "accept", "name"},
					new String[]{"file", Activelink.MIMETYPE, "Activelink"});
			HTMLNode checkBlock = activelinkBox.addChild("p");
			checkBlock.addChild("input", new String[]{"type", "name", "id"},
					new String[]{"checkbox", "ActivelinkDelete", "ActivelinkDelete"});
			checkBlock.addChild("label", "for", "ActivelinkDelete", FlogHelper.getBaseL10n().getString("ActivelinkDeleteFieldDesc"));

			ContentSyntax.addJavascriptEditbox(generalBox, "SmallDescription",
					flog.getShortDescriptionSyntax(), DataFormatter.toString(flog.getShortDescription()),
					FlogHelper.getBaseL10n().getString("SmallDescriptionFieldDesc"));

			final boolean insertPluginStoreDump = flog.shouldPublishStoreDump();
			checkBlock = settingsBox.addChild("p");
			checkBlock.addChild("input", new String[]{"type", "name", "id", insertPluginStoreDump ? "checked" : "class"},
					new String[]{"checkbox", "InsertPluginStoreDump", "InsertPluginStoreDump", insertPluginStoreDump ? "checked" : ""});
			checkBlock.addChild("label", "for", "InsertPluginStoreDump", FlogHelper.getBaseL10n().getString("InsertPluginStoreDumpDesc"));

			// FIXME don't show that until embedded Library search works
			//final boolean insertLibraryIndex = flog.shouldPublishLibraryIndex();
			//checkBlock = settingsBox.addChild("p");
			//checkBlock.addChild("input", new String[]{"type", "name", "id", insertLibraryIndex ? "checked" : "class"},
			//		new String[]{"checkbox", "InsertLibraryIndex", "InsertLibraryIndex", insertLibraryIndex ? "checked" : ""});
			//checkBlock.addChild("label", "for", "InsertLibraryIndex", FlogHelper.getBaseL10n().getString("InsertLibraryIndexDesc"));

			final boolean publishContentModificationDate = flog.shouldPublishDates();
			checkBlock = settingsBox.addChild("p");
			checkBlock.addChild("input", new String[]{"type", "name", "id", publishContentModificationDate ? "checked" : "class"},
					new String[]{"checkbox", "PublishContentModificationDate", "PublishContentModificationDate", publishContentModificationDate ? "checked" : ""});
			checkBlock.addChild("label", "for", "PublishContentModificationDate", FlogHelper.getBaseL10n().getString("PublishContentModificationDateDesc"));

			settingsBox.addChild("p").addChild("label", "for", "NumberOfContentsOnIndex", FlogHelper.getBaseL10n().getString("NumberOfContentsOnIndexFieldDesc")).addChild("br")
					.addChild("input", new String[]{"type", "size", "name", "value"},
					new String[]{"text", "4", "NumberOfContentsOnIndex", Long.toString(flog.getNumberOfContentsOnIndex())});
			settingsBox.addChild("p").addChild("label", "for", "NumberOfContentsOnArchives", FlogHelper.getBaseL10n().getString("NumberOfContentsOnArchivesFieldDesc")).addChild("br")
					.addChild("input", new String[]{"type", "size", "name", "value"},
					new String[]{"text", "4", "NumberOfContentsOnArchives", Long.toString(flog.getNumberOfContentsOnArchives())});
			settingsBox.addChild("p").addChild("label", "for", "SSKPath", FlogHelper.getBaseL10n().getString("SSKPathDesc")).addChild("br")
					.addChild("input", new String[]{"type", "size", "name", "value"},
					new String[]{"text", "20", "SSKPath", flog.getSSKPath()});

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

			final HTMLNode themesBox = new HTMLNode("select", new String[]{"id", "name"}, new String[]{"Theme", "Theme"});
			for (final String theme : FlogFactory.THEMES) {
				final HTMLNode option = themesBox.addChild("option", "value", theme, theme);
				if (flog.getTheme().equals(theme)) {
					option.addAttribute("selected", "selected");
				}
			}

			templatesBox.addChild("p").addChild("label", "for", "Theme", FlogHelper.getBaseL10n().getString("ThemeFieldDesc")).addChild("br").addChild(themesBox);

			final boolean overrideCSS = flog.overrideCSS();
			checkBlock = templatesBox.addChild("p");
			checkBlock.addChild("input", new String[]{"type", "name", "id", overrideCSS ? "checked" : "class"},
					new String[]{"checkbox", "OverrideCSS", "OverrideCSS", overrideCSS ? "checked" : ""});
			checkBlock.addChild("label", "for", "OverrideCSS", FlogHelper.getBaseL10n().getString("OverrideCSSLong"));
			checkBlock.addChild("br");
			checkBlock.addChild("textarea", new String[]{"rows", "cols", "name", "id"},
					new String[]{"12", "80", "OverrideCSSValue", "OverrideCSSValue"}, flog.getCSSOverride());
			checkBlock.addChild("br");
			checkBlock.addChild("a", "href", FlogHelperToadlet.BASE_URI + PreviewToadlet.MY_URI + flog.getID() + "/" + PreviewToadlet.VIEW_DEFAULT_CSS_URI,
					FlogHelper.getBaseL10n().getString("SeeTheDefaultCSS"));


			final HTMLNode buttons = submitBox.addChild("p");
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "Yes", FlogHelper.getBaseL10n().getString("Proceed")});
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "No", FlogHelper.getBaseL10n().getString("Cancel")});

		}
		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}
}
