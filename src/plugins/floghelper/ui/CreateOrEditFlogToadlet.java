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
import plugins.floghelper.data.Activelink;

/**
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
			final PluginStore flog;

			if (FlogHelper.getStore().subStores.containsKey(flogID)) {
				flog = FlogHelper.getStore().subStores.get(flogID);
			} else {
				flog = new PluginStore();
				FlogHelper.getStore().subStores.put(flogID, flog);
			}

			flog.strings.put("ID", flogID);
			flog.strings.put("Title", request.getPartAsString("Title", 100));
			flog.strings.put("DefaultAuthor", request.getPartAsString("DefaultAuthor", 1000));
			flog.strings.put("SmallDescription", request.getPartAsString("SmallDescription", 1000));

			if (request.isPartSet("ActivelinkDelete")) {
				flog.bytesArrays.put("Activelink", null);
			} else if (request.isPartSet("Activelink")) {
				HTTPUploadedFile activelink = request.getUploadedFile("Activelink");
				if (activelink.getContentType() != null) {
					if (activelink.getContentType().toLowerCase().equals(Activelink.MIMETYPE)) {
						BufferedImage imageActivelink = ImageIO.read(activelink.getData().getInputStream());
						if (imageActivelink.getHeight() == Activelink.HEIGHT && imageActivelink.getWidth() == Activelink.WIDTH) {
							flog.bytesArrays.put("Activelink", Activelink.getByteArrayFromUploadedFile(activelink));
							Closer.close(activelink.getData());
						} else {
							final HTMLNode infobox = this.getPM().getInfobox("infobox-error", FlogHelper.getBaseL10n().getString("ActivelinkError"), pageNode.content);
							infobox.addChild("p", FlogHelper.getBaseL10n().getString("ActivelinkInvalidDimensions"));
							final HTMLNode links = infobox.addChild("p");
							links.addChild("a", "href", FlogHelperToadlet.BASE_URI + CreateOrEditFlogToadlet.MY_URI + "?FlogID=" + flogID, FlogHelper.getBaseL10n().getString("ReturnToFlogEdit"));
						}
					} else {
						final HTMLNode infobox = this.getPM().getInfobox("infobox-error", FlogHelper.getBaseL10n().getString("ActivelinkError"), pageNode.content);
						infobox.addChild("p", FlogHelper.getBaseL10n().getString("ActivelinkMustBeAPNGPicture"));
						final HTMLNode links = infobox.addChild("p");
						links.addChild("a", "href", FlogHelperToadlet.BASE_URI + CreateOrEditFlogToadlet.MY_URI + "?FlogID=" + flogID, FlogHelper.getBaseL10n().getString("ReturnToFlogEdit"));
					}
				}
			}

			FlogHelper.putStore();

			final HTMLNode infobox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("FlogCreationSuccessful"), pageNode.content);
			infobox.addChild("p", FlogHelper.getBaseL10n().getString("FlogCreationSuccessfulLong"));
			final HTMLNode links = infobox.addChild("p");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + FlogListToadlet.MY_URI, FlogHelper.getBaseL10n().getString("ReturnToFlogList"));
			links.addChild("br");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + ContentListToadlet.MY_URI + "?FlogID=" + flog.strings.get("ID"), FlogHelper.getBaseL10n().getString("ViewFlogDetails"));
		} else if (request.isPartSet("No")) {
			final HTMLNode infobox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("FlogCreationCancelled"), pageNode.content);
			infobox.addChild("p", FlogHelper.getBaseL10n().getString("FlogCreationCancelledLong"));
			final HTMLNode links = infobox.addChild("p");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + FlogListToadlet.MY_URI, FlogHelper.getBaseL10n().getString("ReturnToFlogList"));
			links.addChild("br");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + CreateOrEditFlogToadlet.MY_URI, FlogHelper.getBaseL10n().getString("CreateNewFlog"));
		} else {
			final String title;
			final PluginStore flog;
			if (flogID == null || flogID.equals("") || !FlogHelper.getStore().subStores.containsKey(flogID)) {
				title = "CreateFlog";
				flogID = DataFormatter.createUniqueFlogID();
				(flog = new PluginStore()).strings.put("ID", flogID);
			} else {
				title = "EditFlog";
				flog = FlogHelper.getStore().subStores.get(flogID);
			}

			final HTMLNode form = FlogHelper.getPR().addFormChild(this.getPM().getInfobox(null,
					FlogHelper.getBaseL10n().getString(title), pageNode.content), this.path(), "CreateOrEdit-" + flogID);

			form.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", flogID});

			form.addChild("p").addChild("label", "for", "Title", FlogHelper.getBaseL10n().getString("TitleFieldDesc")).addChild("br").addChild("input", new String[]{"type", "size", "name", "value"},
					new String[]{"text", "50", "Title", DataFormatter.toString(flog.strings.get("Title"))});

			final HTMLNode authorsBox = new HTMLNode("select", new String[]{"id", "name"}, new String[]{"DefaultAuthorFieldDesc", "DefaultAuthor"});
			for (final String identityID : this.getWoTIdentities().keySet()) {
				final HTMLNode option = authorsBox.addChild("option", "value", identityID, this.getWoTIdentities().get(identityID));
				if (flog.strings.get("DefaultAuthor") != null && flog.strings.get("DefaultAuthor").equals(identityID)) {
					option.addAttribute("selected", "selected");
				}
			}

			form.addChild("p").addChild("label", "for", "DefaultAuthor", FlogHelper.getBaseL10n().getString("DefaultAuthorFieldDesc")).addChild("br").addChild(authorsBox);

			// Most browsers probably won't care about the accept="image/png" attribute
			// But we put it anyway... because it's semantic
			form.addChild("p").addChild("label", "for", "Activelink", FlogHelper.getBaseL10n().getString("ActivelinkFieldDesc")
					.replace("${Width}", Integer.toString(Activelink.WIDTH)).replace("${Height}", Integer.toString(Activelink.HEIGHT)))
					.addChild("br").addChild("input", new String[]{"type", "accept", "name"},
					new String[]{"file", Activelink.MIMETYPE, "Activelink"});
			form.addChild("p").addChild("label", "for", "ActivelinkDelete", FlogHelper.getBaseL10n().getString("ActivelinkDeleteFieldDesc")).addChild("input", new String[]{"type", "name"},
					new String[]{"checkbox", "ActivelinkDelete"});

			form.addChild("p").addChild("label", "for", "SmallDescription", FlogHelper.getBaseL10n().getString("SmallDescriptionFieldDesc")).addChild("br").addChild("textarea", new String[]{"rows", "cols", "name"},
					new String[]{"12", "80", "SmallDescription"}, DataFormatter.toString(flog.strings.get("SmallDescription")));

			final HTMLNode buttons = form.addChild("p");
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "Yes", FlogHelper.getBaseL10n().getString("Proceed")});
			buttons.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "No", FlogHelper.getBaseL10n().getString("Cancel")});

		}
		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}
}
