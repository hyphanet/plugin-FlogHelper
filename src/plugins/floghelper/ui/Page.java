/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins.floghelper.ui;

import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.pluginmanager.PluginHTTPException;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;
import plugins.floghelper.FlogHelper;

/**
 *
 * @author romain
 */
public abstract class Page {
	public static final String URI_COMMON = "/plugins/plugins.floghelper.FlogHelper";

	public static String handleHTTPGet(final HTTPRequest request) throws PluginHTTPException {
		PageNode pn = FlogHelper.getPR().getPageMaker().getPageNode(URI_COMMON, null);
		Page.getPage(request).getContent(pn.content, request);
		return pn.outer.generate();
	}

	public static String handleHTTPPost(final HTTPRequest request) throws PluginHTTPException {
		return Page.getPage(request).getContent(new HTMLNode("div", "id", "content"), request).generate();
	}

	public static Page getPage(final HTTPRequest request) {
		String realURI = request.getPath().split("\\?")[0].replace(URI_COMMON, "");

		if(realURI.equals("/") || realURI.equals("")) {
			return new WelcomePage();
		}

		else throw new UnsupportedOperationException("404 !");
	}

	public abstract HTMLNode getContent(final HTMLNode content, final HTTPRequest request);
}
