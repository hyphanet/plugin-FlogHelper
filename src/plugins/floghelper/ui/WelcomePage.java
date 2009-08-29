/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins.floghelper.ui;

import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;
import plugins.floghelper.FlogHelper;

/**
 *
 * @author romain
 */
public class WelcomePage extends Page {

	@Override
	public HTMLNode getContent(HTMLNode content, HTTPRequest request) {
		if(FlogHelper.getFlogs().isEmpty()) {
			content.addChild("p", FlogHelper.getBaseL10n().getString("NoFlogsYet"));
		} else content.addChild("p", DataFormatter.printFlogContainer(FlogHelper.getFlogs().get(0)));

		return content;
	}
}
