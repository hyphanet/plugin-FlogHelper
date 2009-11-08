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
import freenet.support.SizeUtil;
import freenet.support.api.HTTPRequest;
import freenet.support.api.HTTPUploadedFile;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.TreeMap;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.data.Activelink;

/**
 * This toadlet handles Flog's attachments.
 * 
 * @author Artefact2
 */
public class AttachementsToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/Attachements/";

	public AttachementsToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	public void getPageGet(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final PluginStore flog = FlogHelper.getStore().subStores.get(this.getParameterWhetherItIsPostOrGet(request, "FlogID", 7));
		if (flog == null) {
			this.sendErrorPage(ctx, 404, "Not found", "Incorrect or missing FlogID.");
			return;
		}

		final HTMLNode warning = this.getPM().getInfobox("infobox-warning", FlogHelper.getBaseL10n().getString("Warning"), pageNode.content);
		warning.addChild("p", FlogHelper.getBaseL10n().getString("KeepAttachementsAsSmallAsPossibleLong"));

		final HTMLNode insertBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("NewAttachement"), pageNode.content);
		final HTMLNode formUpload = FlogHelper.getPR().addFormChild(insertBox, this.path(), "NewAttachement");
		HTMLNode inForm = formUpload.addChild("p");
		inForm.addAttribute("method", "post");
		inForm.addChild("label", "for", "AttachementFile", FlogHelper.getBaseL10n().getString("AttachementToUpload"));
		inForm.addChild("br");
		inForm.addChild("input", new String[]{"type", "name"}, new String[]{"file", "AttachementFile"});
		inForm.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "Yes", FlogHelper.getBaseL10n().getString("Proceed")});
		inForm.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", flog.strings.get("ID")});

		if(!flog.subStores.containsKey("Attachements")) {
			flog.subStores.put("Attachements", new PluginStore());
		}

		final PluginStore attachements = flog.subStores.get("Attachements");

		final HTMLNode table = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("ListOfAttachementsOf").replace("${FlogName}",
				DataFormatter.htmlSpecialChars(flog.strings.get("Title"))), pageNode.content).addChild("table");

		final HTMLNode tHead = table.addChild("thead");
		final HTMLNode tFoot = table.addChild("tfoot");
		final HTMLNode tBody = table.addChild("tbody");

		final HTMLNode actionsRow = new HTMLNode("tr");

		final HTMLNode formBack = FlogHelper.getPR().addFormChild(actionsRow.addChild("th", "colspan", "5"), FlogHelperToadlet.BASE_URI +
				FlogListToadlet.MY_URI, "BackToFlogList");
		formBack.addAttribute("method", "get");
		formBack.addChild("input", new String[]{"type", "value"},
				new String[]{"submit", FlogHelper.getBaseL10n().getString("BackToFlogList")});

		final HTMLNode headersRow = new HTMLNode("tr");
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("Filename"));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("CreationDate") + " \u25BC");
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("Size"));
		headersRow.addChild("th", "colspan", "2", FlogHelper.getBaseL10n().getString("Actions"));

		tHead.addChild(actionsRow);
		tHead.addChild(headersRow);
		tFoot.addChild(headersRow);

		if (attachements.subStores.isEmpty()) {
			tBody.addChild("tr").addChild("td", "colspan", "5", FlogHelper.getBaseL10n().getString("NoAttachementsYet"));
		}

		TreeMap<Long, PluginStore> sortedAttachments = new TreeMap<Long, PluginStore>();
		for (final PluginStore attachement : attachements.subStores.values()) {
			sortedAttachments.put(attachement.longs.get("CreationDate"), attachement);
		}
		
		for (final PluginStore attachement : sortedAttachments.descendingMap().values()) {
			final HTMLNode row = tBody.addChild("tr");
			row.addChild("td").addChild("pre", DataFormatter.toString(attachement.strings.get("Filename")));
			row.addChild("td", DataFormatter.toString(DataFormatter.LocalDateFormatter.format(new Date(attachement.longs.get("CreationDate")))));
			row.addChild("td", SizeUtil.formatSize(attachement.bytesArrays.get("Content").length, true));

			final HTMLNode formPreview = FlogHelper.getPR().addFormChild(row.addChild("td"), FlogHelperToadlet.BASE_URI +
					PreviewToadlet.MY_URI + flog.strings.get("ID") + "/" + attachement.strings.get("Filename"), "PreviewAttachement-" + attachement.strings.get("Filename"));
			formPreview.addAttribute("method", "get");
			formPreview.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Preview")});

			final HTMLNode formDelete = FlogHelper.getPR().addFormChild(row.addChild("td"), this.path(),
					"DeleteAttachement-" + attachement.strings.get("Filename"));
			formDelete.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Delete")});
			formDelete.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "AttachementToDelete", DataFormatter.toString(attachement.strings.get("Filename"))});
			formDelete.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.strings.get("ID"))});
		}

		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}

	public void getPagePost(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final PluginStore flog = FlogHelper.getStore().subStores.get(this.getParameterWhetherItIsPostOrGet(request, "FlogID", 7));
		if (flog == null) {
			this.sendErrorPage(ctx, 404, "Not found", "Incorrect or missing FlogID.");
		}

		if (request.isPartSet("AttachementFile")) {
			HTTPUploadedFile newAttachement = request.getUploadedFile("AttachementFile");
			if(!newAttachement.getFilename().equals("") && newAttachement.getData().size() > 0) {
				final PluginStore attachement = new PluginStore();
				final String id = DataFormatter.createSubStoreUniqueID(flog.subStores.get("Attachements"));
				final String filename = "Att-" + id + "-" + newAttachement.getFilename();
				attachement.strings.put("Filename", filename);
				attachement.longs.put("CreationDate", System.currentTimeMillis());
				attachement.bytesArrays.put("Content", Activelink.getByteArrayFromUploadedFile(newAttachement));
				flog.subStores.get("Attachements").subStores.put(filename, attachement);
				FlogHelper.putStore();
			}
		} else {

			final String nameToDelete = request.getPartAsString("AttachementToDelete", 1000);
			final String nameToReallyDelete = request.getPartAsString("AttachementToReallyDelete", 1000);

			if (nameToReallyDelete != null && !nameToReallyDelete.equals("")) {
				if (request.getPartAsString("Yes", 3).equals("Yes")) {

					flog.subStores.get("Attachements").subStores.remove(nameToReallyDelete);
					FlogHelper.putStore();
					this.handleMethodGET(uri, request, ctx);
					return;
				} else {
					this.handleMethodGET(uri, request, ctx);
					return;
				}
			}

			if (nameToDelete != null && !nameToDelete.equals("")) {
				final HTMLNode confirm = this.getPM().getInfobox("infobox-alert", FlogHelper.getBaseL10n().getString("ReallyDelete"), pageNode.content);
				final HTMLNode form = FlogHelper.getPR().addFormChild(confirm, this.path(), "ReallyDelete-" + nameToDelete);
				form.addChild("p", FlogHelper.getBaseL10n().getString("ReallyDeleteAttachementLong").replace("${AttachementName}", nameToDelete));
				final HTMLNode buttons = form.addChild("p");
				buttons.addChild("input", new String[]{"type", "name", "value"},
						new String[]{"hidden", "AttachementToReallyDelete", nameToDelete});
				buttons.addChild("input", new String[]{"type", "name", "value"},
						new String[]{"hidden", "FlogID", flog.strings.get("ID")});
				buttons.addChild("input", new String[]{"type", "name", "value"},
						new String[]{"submit", "Yes", FlogHelper.getBaseL10n().getString("Yes")});
				buttons.addChild("input", new String[]{"type", "name", "value"},
						new String[]{"submit", "No", FlogHelper.getBaseL10n().getString("No")});

				writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
				return;
			}
		}

		this.handleMethodGET(uri, request, ctx);
	}
}
