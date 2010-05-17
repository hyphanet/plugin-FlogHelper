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
package plugins.floghelper.contentsyntax;

import freenet.support.HTMLNode;
import freenet.support.Logger;
import java.util.TreeMap;
import java.util.Vector;
import plugins.floghelper.contentsyntax.js.JavascriptFactoryToadlet;
import plugins.floghelper.ui.FlogHelperToadlet;

/**
 * This abstract class represent a content syntax, such as xHTML, BBCode, ...
 *
 * @author Artefact2
 */
public abstract class ContentSyntax {

	/**
	 * List of currently implemented syntaxes, the names should be equal to
	 * their respective classnames.
	 */
	public static final TreeMap<String, String> Syntaxes;

	static {
		Syntaxes = new TreeMap<String, String>();
		Syntaxes.put("YAWKL", "Simplified markup");
		Syntaxes.put("RawXHTML", "xHTML");
	}

	/**
	 * List of SyntaxElements. Basically : elements ([bold], [/bold]), its
	 * associated regex for parsing and more informations for javascript buttons.
	 */
	protected final Vector<SyntaxElement> syntaxElements = new Vector<SyntaxElement>();

	/**
	 * Parse a string into xHTML. You can override this if you want, but
	 * remember to call super.parseSomeString() too to avoid code
	 * duplication.
	 *
	 * @param s String to parse.
	 * @return Formatted string (aka xHTML code)
	 */
	public String parseSomeString(String s) {
		for (SyntaxElement e : this.syntaxElements) {
			s = e.regex.matcher(s).replaceAll(e.xHTMLReplacement);
		}
		return s;
	}

	/**
	 * Get the javascript code used to provide the buttons to automatically add
	 * the elements in the textarea.
	 *
	 * @param textAreaName Name of the textarea used.
	 * @return Javascript code.
	 */
	public static String getJavascriptCode(String textAreaName) {
		StringBuilder s = new StringBuilder();

		s.append("function refreshSyntaxButtons_" + textAreaName + "() {\n");
		s.append("	var currentSyntaxList_" + textAreaName + " = document.getElementById(\"" + textAreaName + "_syntaxes\");\n");
		s.append("	currentSyntax_" + textAreaName + " = currentSyntaxList_" + textAreaName + " = currentSyntaxList_" + textAreaName +
				".options[currentSyntaxList_" + textAreaName + ".selectedIndex].value;\n");
		s.append("	var buttonsDiv = document.getElementById(\"" + textAreaName + "_buttons\");\n" +
				"	while(buttonsDiv.hasChildNodes()) { buttonsDiv.removeChild(buttonsDiv.firstChild); }\n");

		for (String e : ContentSyntax.Syntaxes.keySet()) {
			s.append("	if(currentSyntax_" + textAreaName + " == \"" + e.toString() + "\") {\n");

			int i = 0;
			try {
				ContentSyntax eCS = (ContentSyntax) Class.forName("plugins.floghelper.contentsyntax." + e.toString()).newInstance();
				for (SyntaxElement eSE : eCS.syntaxElements) {
					if (!eSE.isMajor) {
						continue;
					}

					s.append("		var button" + i + " = document.createElement(\"input\");\n" +
							"		button" + i + ".setAttribute(\"type\", \"button\");\n" +
							"		button" + i + ".setAttribute(\"value\", \"" + eSE.name + "\");\n" +
							"		button" + i + ".addEventListener(\"click\", function(event){ addSomething(\"" + eSE.beginThing.replace("\"", "\\\"") + "\", \"" + eSE.endThing.replace("\"", "\\\"") +
							"\", \"" + textAreaName + "\"); }, false);\n" +
							"		buttonsDiv.appendChild(button" + i + ")\n");
					++i;
				}
			} catch (InstantiationException ex) {
				Logger.error(ContentSyntax.class, "Could NOT instanciate ContentSyntax " + e.toString());
			} catch (IllegalAccessException ex) {
				Logger.error(ContentSyntax.class, "Could NOT instanciate ContentSyntax " + e.toString());
			} catch (ClassNotFoundException ex) {
				Logger.error(ContentSyntax.class, "Could NOT instanciate ContentSyntax " + e.toString());
			}

			s.append("	}\n");
		}

		return s.append("}\n").toString();
	}

	/**
	 * Add a textarea and its associated javascript stuff.
	 *
	 * @param parentForm Parent form to use.
	 * @param textAreaName Name/id of the textarea.
	 * @param defaultSelectedValue Default ContentSyntax to select.
	 * @param defaultContent Initial value of the textarea.
	 * @param textAreaLabel Label above the textarea.
	 * @return HTMLNode of the parentForm.
	 */
	public static HTMLNode addJavascriptEditbox(HTMLNode parentForm, String textAreaName, String defaultSelectedValue, String defaultContent, String textAreaLabel) {
		parentForm.addChild("script", new String[]{"type", "src"}, new String[]{"text/javascript",
					FlogHelperToadlet.BASE_URI + JavascriptFactoryToadlet.MY_URI + "EditBox.js"}, " ");

		parentForm.addChild("script", new String[]{"type", "src"}, new String[]{"text/javascript",
					FlogHelperToadlet.BASE_URI + JavascriptFactoryToadlet.MY_URI + "RefreshSyntaxButtons-" + textAreaName + ".js"}, " ");

		parentForm = parentForm.addChild("p");
		parentForm.addChild("#", textAreaLabel);
		parentForm.addChild("br");

		final HTMLNode syntaxesList = parentForm.addChild(
				"select", new String[]{"id", "name", "onchange"}, new String[]{textAreaName + "_syntaxes", textAreaName + "_syntaxes",
					"refreshSyntaxButtons_" + textAreaName + "();"});
		for (String e : ContentSyntax.Syntaxes.keySet()) {
			HTMLNode syntaxListElement = syntaxesList.addChild("option", "value", e.toString(), Syntaxes.get(e));
			if (defaultSelectedValue != null && defaultSelectedValue.equals(e.toString())) {
				syntaxListElement.addAttribute("selected", "selected");
			}
		}

		parentForm.addChild("br").addChild("span", new String[]{"id", "style"}, new String[]{textAreaName + "_buttons", "display: inline-block; width: 650px;"}, " ");
		parentForm.addChild("br");
		parentForm.addChild("textarea", new String[]{"rows", "cols", "name", "id"},
				new String[]{"12", "80", textAreaName, textAreaName}, defaultContent);

		parentForm.addChild("script", "type", "text/javascript", "refreshSyntaxButtons_" + textAreaName + "();");

		return parentForm;
	}

	public static String parseSomeString(String content, String syntaxName) {
		try {
			return ((ContentSyntax) Class.forName("plugins.floghelper.contentsyntax." + syntaxName).newInstance()).parseSomeString(content);
		} catch (ClassNotFoundException ex) {
			Logger.error(ContentSyntax.class, "Cannot parse using content syntax: " + syntaxName);
		} catch (InstantiationException ex) {
			Logger.error(ContentSyntax.class, "Cannot parse using content syntax: " + syntaxName);
		} catch (IllegalAccessException ex) {
			Logger.error(ContentSyntax.class, "Cannot parse using content syntax: " + syntaxName);
		}

		return "";
	}
}
