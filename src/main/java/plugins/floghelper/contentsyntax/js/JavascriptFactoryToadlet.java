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
package plugins.floghelper.contentsyntax.js;

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.MultiValueTable;
import freenet.support.api.HTTPRequest;
import java.io.IOException;
import java.net.URI;
import plugins.floghelper.contentsyntax.ContentSyntax;
import plugins.floghelper.ui.FlogHelperToadlet;

/**
 * This toadlet sends raw Javascript code with the appropriate MIME type to the
 * browser.
 *
 * @author Artefact2
 */
public class JavascriptFactoryToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/JavascriptFactory/";

	public JavascriptFactoryToadlet(HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	@Override
	public void getPageGet(PageNode pageNode, URI uri, HTTPRequest request, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		String jsCode = "";

		String[] partsWanted = uri.toString().split("/");
		String partWanted = partsWanted[partsWanted.length - 1];

		if(partWanted.startsWith("RefreshSyntaxButtons-")) {
			String syntaxToPut = partWanted.replace("RefreshSyntaxButtons-", "").replace(".js", "");
			jsCode = ContentSyntax.getJavascriptCode(syntaxToPut);
		} else if (partWanted.equals("EditBox.js")) {
			jsCode = "function addSomething(begin, end, textareaid) {\n" +
					"	var textarea = document.getElementById(textareaid);\n" +
					"\n" +
					"	// We keep the scroll, because annoying scripts that reset the scroll\n" +
					"	// to zero everytime are just so... annoying.\n" +
					"	var scrollLeft = textarea.scrollLeft;\n" +
					"	var scrollTop = textarea.scrollTop;\n" +
					"\n" +
					"	var before = textarea.value.substring(0, textarea.selectionStart);\n" +
					"	var current = textarea.value.substring(textarea.selectionStart, textarea.selectionEnd);\n" +
					"	var after = textarea.value.substring(textarea.selectionEnd);\n" +
					"\n" +
					"	textarea.value = before + begin + current + end + after;\n" +
					"	textarea.setSelectionRange(before.length + begin.length, before.length + begin.length + current.length);\n" +
					"	textarea.focus();\n" +
					"	textarea.scrollTop = scrollTop;\n" +
					"	textarea.scrollLeft = scrollLeft;\n" +
					"}\n";
		}

		byte[] data = jsCode.getBytes("UTF-8");
		ctx.sendReplyHeaders(200, "OK", new MultiValueTable<String, String>(), "text/javascript", data.length);
		ctx.writeData(data);
	}

	@Override
	public void getPagePost(PageNode pageNode, URI uri, HTTPRequest request, ToadletContext ctx) throws ToadletContextClosedException, IOException {

	}

}
