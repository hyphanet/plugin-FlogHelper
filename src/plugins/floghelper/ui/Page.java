/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins.floghelper.ui;

import freenet.pluginmanager.PluginHTTPException;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

/**
 *
 * @author romain
 */
public abstract class Page {
	public static final String URI_COMMON = "/plugins/plugins.floghelper.FlogHelper";

	public static String handleHTTPGet(final HTTPRequest request) throws PluginHTTPException {
		return Page.getPage(request).getContent(new HTMLNode("div", "id", "content"), request).generate();
	}

	public static String handleHTTPPost(final HTTPRequest request) throws PluginHTTPException {
		return Page.getPage(request).getContent(new HTMLNode("div", "id", "content"), request).generate();
	}

	public static Page getPage(final HTTPRequest request) {
		return new WelcomePage();
	}

	public abstract HTMLNode getContent(final HTMLNode content, final HTTPRequest request);
}
