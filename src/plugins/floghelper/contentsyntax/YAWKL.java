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

import java.util.regex.Pattern;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.data.DataFormatter;

/**
 * YAWKL stands for "Yet Another WiKicode Like language"...
 * Goals : easy to type, easy to remember, easy to parse.
 *
 * @author Artefact2
 */
public class YAWKL extends ContentSyntax {
	public YAWKL() {
		// Very basic formatting stuff
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Bold"), "**", "**",
				SyntaxElement.begin + "(.+?)" + SyntaxElement.end,
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
				"<strong>$1</strong>", true));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Italic"), "''", "''",
				"&#39;&#39;(.+?)&#39;&#39;",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
				"<em>$1</em>", true));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Underline"), "__", "__",
				SyntaxElement.begin + "(.+?)" + SyntaxElement.end,
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
				"<span style=\"text-decoration: underline;\">$1</span>", true));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Strike"), "---", "---",
				SyntaxElement.begin + "(.+?)" + SyntaxElement.end,
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
				"<span style=\"text-decoration: line-through;\">$1</span>", true));

		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Subscript"), "_[[", "]]",
				SyntaxElement.begin + "(.+?)" + SyntaxElement.end,
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
				"<sub>$1</sub>", true));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Superscript"), "^^^[[", "]]",
				SyntaxElement.begin + "(.+?)" + SyntaxElement.end,
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
				"<sup>$1</sup>", true));

		// Headings (from 1 to 6)
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H1"), "====== ", " ======",
				"^\\s*" + SyntaxElement.begin + "\\s*(.+?)\\s*" + SyntaxElement.end + "\\s*$",
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
				"</p><h1>$1</h1><p>", true));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H2"), "===== ", " =====",
				"^\\s*" + SyntaxElement.begin + "\\s*(.+?)\\s*" + SyntaxElement.end + "\\s*$",
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
				"</p><h2>$1</h2><p>", true));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H3"), "==== ", " ====",
				"^\\s*" + SyntaxElement.begin + "\\s*(.+?)\\s*" + SyntaxElement.end + "\\s*$",
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
				"</p><h3>$1</h3><p>", true));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H4"), "=== ", " ===",
				"^\\s*" + SyntaxElement.begin + "\\s*(.+?)\\s*" + SyntaxElement.end + "\\s*$",
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
				"</p><h4>$1</h4><p>", true));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H5"), "== ", " ==",
				"^\\s*" + SyntaxElement.begin + "\\s*(.+?)\\s*" + SyntaxElement.end + "\\s*$",
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
				"</p><h5>$1</h5><p>", true));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H6"), "= ", " =",
				"^\\s*" + SyntaxElement.begin + "\\s*(.+?)\\s*" + SyntaxElement.end + "\\s*$",
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
				"</p><h6>$1</h6><p>", true));

		// Pictures
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Picture"), "[[picture|", "]]",
				SyntaxElement.begin + "(.+?)" + SyntaxElement.end,
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
				"<img alt=\"Image\" src=\"$1\" />", true));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Picture"), "[[", "]]",
				SyntaxElement.begin + "((.+?)\\.(jpe?g|png|gif|svg|bmp|tiff?))" + SyntaxElement.end,
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
				"<img alt=\"Image\" src=\"$1\" />", false));

		// Audio
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Audio"), "[[audio|", "]]",
				SyntaxElement.begin + "(.+?)" + SyntaxElement.end,
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
				"<audio controls=\"controls\" preload=\"auto\" type=\"audio/mpeg\" style=\"height: 20px;\" src=\"$1\" />", true));

		// Video
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Video"), "[[video|", "]]",
				SyntaxElement.begin + "(.+?)" + SyntaxElement.end,
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
				"<video controls=\"controls\" style=\"height: 300px;\" src=\"$1?type=video/ogg\" />", true));

		// Size and color
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Size"), "[[1em|", "]]",
				"\\[\\[((([0-9]|\\.)+)(px|em|pt))\\|(.+?)\\]\\]",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
				"<span style=\"font-size: $1;\">$5</span>", true));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Color"), "[[#000000|", "]]",
				"\\[\\[(#[0-9a-fA-F]+)\\|(.+?)\\]\\]",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
				"<span style=\"color: $1;\">$2</span>", true));

		// Text align
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Center"), "[[><]]", "",
				"\\[\\[&gt;&lt;\\]\\](.+?)(\n\\s*\n|\\s*</p>)",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
				"</p><p style=\"text-align: center;\">$1</p>\n<p>$2", true));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Left"), "[[<<]]", "",
				"\\[\\[&lt;&lt;\\]\\](.+?)(\n\\s*\n|\\s*</p>)",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
				"</p><p style=\"text-align: left;\">$1</p>\n<p>", true));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Right"), "[[>>]]", "",
				"\\[\\[&gt;&gt;\\]\\](.+?)(\n\\s*\n|\\s*</p>)",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
				"</p><p style=\"text-align: right;\">$1</p>\n<p>", true));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Justify"), "[[<>]]", "",
				"\\[\\[&lt;&gt;\\]\\](.+?)(\n\\s*\n|\\s*</p>)",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
				"</p><p style=\"text-align: justify;\">$1</p>\n<p>", true));

		// Lists
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("ListElement"), "", "",
				SyntaxElement.begin + "^([0-9]+(\\.)?|(\\*|o|-))\\s+(.+?)$" + SyntaxElement.end,
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
				"<li>$4</li>", false));

		// Code and quote
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Code"), "[[code|", "]]",
				SyntaxElement.begin + "(.+?)" + SyntaxElement.end,
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
				"</p><code><p>$1</p></code><p>", true));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Quote"), "[[quote|", "]]",
				SyntaxElement.begin + "(.+?)" + SyntaxElement.end,
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
				"</p><blockquote><p>$1</p></blockquote><p>", true));

		// Links
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("FreenetKey"), "[[", "]]",
				SyntaxElement.begin + "(.+?)\\|(https?://[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}(:[0-9]{2,5})?)?/?((USK|SSK|CHK|KSK)@(.+?))" + SyntaxElement.end,
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
				"<a href=\"/$4\">$1</a>", false));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("FreenetKey"), "[[", "]]",
				SyntaxElement.begin + "(https?://[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}(:[0-9]{2,5})?)?/?((USK|SSK|CHK|KSK)@(.+?))" + SyntaxElement.end,
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
				"<a href=\"/$3\">$3</a>", false));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Link"), "[[Link title|", "]]",
				"\\[\\[(.+?)\\|(.+?)\\]\\]",
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
				"<a href=\"$1\">$2</a>", true));
		this.syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Link"), "[[", "]]",
				SyntaxElement.begin + "(.+?)" + SyntaxElement.end,
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
				"<a href=\"$1\">$1</a>", false));
	}

	@Override
	public String parseSomeString(String s) {
		// Don't allow xHTML tags.
		s = DataFormatter.htmlSpecialChars(s);

		s = "<p>\n" + s + "\n</p>";

		s = super.parseSomeString(s);

		// Make the generated xHTML code prettier and generally valid
		// Should work most of the time.

		// Use <br /> for new lines but don't put them before </p>
		s = s.replace("\n", "<br />\n").replaceAll("<br />\\s*<br />", "\n</p>\n<p>");

		// Remove empty paragraphes
		s = s.replaceAll("<p>(\\s*(<br />)*\\s*)*</p>", "");

		// <p><br /> is ugly, fix it
		s = s.replaceAll("<p>\\s*<br />", "<p>");

		// Remove "<br />"s that aren't in a paragraph element
		s = s.replaceAll("</p>\\s*<br />", "</p>");

		// <br /></p> is ugly, fix it
		s = s.replaceAll("<br />\\s*</p>", "</p>");

		// Fix </li><br />
		s = s.replaceAll("</li>\\s*<br />", "</li>");

		// Add <ul> when necessary
		s = s.replaceAll("<p>\\s*<li>", "<ul>\n<li>");
		s = s.replaceAll("<br />\\s*\n<li>", "</p><ul>\n<li>");
		s = s.replaceAll("</li>\\s*</p>|</p>\\s*</li>", "</li></ul>");

		// Unescape escaped formatting characters, eg \'\' -> ''
		s = s.replace("\\*\\*", "**");
		s = s.replace("\\'\\'", "''");
		s = s.replace("\\_\\_", "__");
		s = s.replace("\\-\\-\\-", "---");
		s = s.replace("\\[\\[", "[[");
		s = s.replace("\\]\\]", "]]");
		s = s.replace("\\=", "=");

		return s;
	}
}
