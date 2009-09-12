/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.contentsyntax;

import plugins.floghelper.FlogHelper;

/**
 * RawXHTML is raw xHTML code. It does nothing.
 * @author Artefact2
 */
public class RawXHTML extends ContentSyntax {

	public RawXHTML() {
		// Very basic formatting stuff
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Bold"), "<strong>", "</strong>"));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Italic"), "<em>", "</em>"));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Underline"), "<span style=\"text-decoration: underline;\">", "</span>"));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Strike"), "<span style=\"text-decoration: line-through;\">", "</span>"));

		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Subscript"), "<sub>", "</sub>"));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Superscript"), "<sup>", "</sup>"));

		// Headings (from 1 to 6)
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H1"), "</p><h1>", "</h1><p>"));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H2"), "</p><h2>", "</h2><p>"));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H3"), "</p><h3>", "</h3><p>"));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H4"), "</p><h4>", "</h4><p>"));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H5"), "</p><h5>", "</h5><p>"));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H6"), "</p><h6>", "</h6><p>"));

		// Pictures
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Picture"), "<img alt=\"Image\" src=\"", "\" />"));

		// Size and color
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Size"), "<span style=\"font-size: 1em;\">", "</span>"));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Color"), "<span style=\"color: #000000;\">", "</span>"));

		// Text align
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Center"), "</p><p style=\"text-align: center;\">", "</p><p>"));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Left"), "</p><p style=\"text-align: left;\">", "</p><p>"));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Right"), "</p><p style=\"text-align: right;\">", "</p><p>"));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Justify"), "</p><p style=\"text-align: justify;\">", "</p><p>"));

		// Code and quote
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Code"), "</p><code><p>", "</p></code><p>"));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Quote"), "</p><blockquote><p>", "</p></blockquote><p>"));

		// Links
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Link"), "<a href=\"", "\">Link text</a>"));
	}

	@Override
	public String parseSomeString(String s) {
		return s;
	}
}
