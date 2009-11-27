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
import freenet.support.SizeUtil;
import freenet.support.api.HTTPRequest;
import freenet.support.api.HTTPUploadedFile;
import java.io.IOException;
import java.net.URI;
import java.util.TreeMap;
import java.util.Vector;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.data.Activelink;
import plugins.floghelper.data.Attachment;
import plugins.floghelper.data.Flog;
import plugins.floghelper.data.pluginstore.PluginStoreFlog;

/**
 * This toadlet handles Flog's attachments.
 * 
 * @author Artefact2
 */
public class AttachmentsToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/Attachments/";

	public AttachmentsToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	public void getPageGet(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final Flog flog = new PluginStoreFlog(this.getParameterWhetherItIsPostOrGet(request, "FlogID", 7));

		final HTMLNode warning = this.getPM().getInfobox("infobox-warning", FlogHelper.getBaseL10n().getString("Warning"), pageNode.content);
		warning.addChild("p", FlogHelper.getBaseL10n().getString("KeepAttachmentsAsSmallAsPossibleLong"));

		final HTMLNode insertBox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("NewAttachment"), pageNode.content);
		final HTMLNode formUpload = FlogHelper.getPR().addFormChild(insertBox, this.path(), "NewAttachment");
		HTMLNode inForm = formUpload.addChild("p");
		inForm.addAttribute("method", "post");
		inForm.addChild("label", "for", "AttachmentFile", FlogHelper.getBaseL10n().getString("AttachmentToUpload"));
		inForm.addChild("br");
		inForm.addChild("input", new String[]{"type", "name"}, new String[]{"file", "AttachmentFile"});
		inForm.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"submit", "Yes", FlogHelper.getBaseL10n().getString("Proceed")});
		inForm.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", flog.getID()});

		final Vector<Attachment> attachments = flog.getAttachments();

		final HTMLNode table = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("ListOfAttachmentsOf").replace("${FlogName}",
				DataFormatter.htmlSpecialChars(flog.getTitle())), pageNode.content).addChild("table");

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

		if (attachments.isEmpty()) {
			tBody.addChild("tr").addChild("td", "colspan", "5", FlogHelper.getBaseL10n().getString("NoAttachmentsYet"));
		}

		TreeMap<Long, Attachment> sortedAttachments = new TreeMap<Long, Attachment>();
		for (final Attachment attachment : attachments) {
			sortedAttachments.put(attachment.getInsertionDate().getTime(), attachment);
		}
		
		Attachment[] alist = sortedAttachments.values().toArray(new Attachment[sortedAttachments.size()]);
		for (int i=alist.length-1;i>=0;i--) {
			Attachment attachment = alist[i];
			final HTMLNode row = tBody.addChild("tr");
			row.addChild("td").addChild("pre", DataFormatter.toString(attachment.getName()));
			row.addChild("td", DataFormatter.toString(DataFormatter.LocalDateFormatter.format(attachment.getInsertionDate())));
			row.addChild("td", SizeUtil.formatSize(attachment.getData().length, true));

			final HTMLNode formPreview = FlogHelper.getPR().addFormChild(row.addChild("td"), FlogHelperToadlet.BASE_URI +
					PreviewToadlet.MY_URI + flog.getID() + "/" + attachment.getName(), "PreviewAttachment-" + attachment.getName());
			formPreview.addAttribute("method", "get");
			formPreview.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Preview")});

			final HTMLNode formDelete = FlogHelper.getPR().addFormChild(row.addChild("td"), this.path(),
					"DeleteAttachment-" + attachment.getName());
			formDelete.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Delete")});
			formDelete.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "AttachmentToDelete", DataFormatter.toString(attachment.getName())});
			formDelete.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.getID())});
		}

		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}

	public void getPagePost(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final Flog flog = new PluginStoreFlog(this.getParameterWhetherItIsPostOrGet(request, "FlogID", 7));

		if (request.isPartSet("AttachmentFile")) {
			HTTPUploadedFile newAttachment = request.getUploadedFile("AttachmentFile");
			if(!newAttachment.getFilename().equals("") && newAttachment.getData().size() > 0) {
				final Attachment attachment = flog.newAttachment("", Activelink.getByteArrayFromUploadedFile(newAttachment));
				attachment.setName("Att-" + attachment.getID() + "-" + newAttachment.getFilename());
				flog.putAttachment(attachment);
				FlogHelper.putStore();
			}
		} else {

			final String nameToDelete = request.getPartAsString("AttachmentToDelete", 1000);
			final String nameToReallyDelete = request.getPartAsString("AttachmentToReallyDelete", 1000);

			if (nameToReallyDelete != null && !nameToReallyDelete.equals("")) {
				if (request.getPartAsString("Yes", 3).equals("Yes")) {

					flog.deleteAttachment(nameToReallyDelete);
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
				form.addChild("p", FlogHelper.getBaseL10n().getString("ReallyDeleteAttachmentLong").replace("${AttachmentName}", nameToDelete));
				final HTMLNode buttons = form.addChild("p");
				buttons.addChild("input", new String[]{"type", "name", "value"},
						new String[]{"hidden", "AttachmentToReallyDelete", nameToDelete});
				buttons.addChild("input", new String[]{"type", "name", "value"},
						new String[]{"hidden", "FlogID", flog.getID()});
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
