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
import plugins.floghelper.data.DataFormatter;

/**
 * Immutable object representing a syntax base element. Examples : bold text,
 * headings, ordered list, ...
 *
 * @author Artefact2
 */
public class SyntaxElement {
	public static final String begin = "___${BEGIN}___";
	public static final String end = "___${END}___";

	/**
	 * Name of this element, this will be the label of the button above the editbox.
	 */
	public final String name;
	/**
	 * Begin of the element, example [bold]. This will be inserted before the textarea's
	 * selection when the user clicks the right button.
	 */
	public final String beginThing;
	/**
	 * End of the element, exampe [/bold]. This will be inserted after the textarea's
	 * selection when the user clicks the right button.
	 */
	public final String endThing;
	/**
	 * Most of the time you want to use beginThing and endThing in the Regex
	 * to make it more flexible, but with some special elements you will have
	 * to use a fully custom regex.
	 */
	public final Pattern regex;
	/**
	 * xHTML replacement of this element, eg. the second parameter of the regex in
	 * string.replaceAll().
	 */
	public final String xHTMLReplacement;
	/**
	 * If false, it won't be shown in the help and in the JS editbox, but it will
	 * still be parsed when parseSomeString() is called.
	 */
	public final boolean isMajor;

	/**
	 * Create a new, simple syntaxElement.
	 *
	 * @param name Name of this element.
	 * @param beginThing Begin delimiter of the element.
	 * @param endThing End delimiter of the element.
	 */
	public SyntaxElement(final String name, final String beginThing, final String endThing) {
		this(name, beginThing, endThing, "", 0, "", true);
	}

	/**
	 * Create a new syntaxElement with all its parameters customized.
	 *
	 * @param name Name of this element.
	 * @param beginThing Begin delimiter of the element.
	 * @param endThing End delimiter of the element.
	 * @param regex Regex used for parsing.
	 * @param regexFlags Flags to use, eg. Pattern.MULTILINE, ...
	 * @param xHTMLReplacement xHTML replacement of this element.
	 * @param isMajor If true this element will have a dedicated button above a textarea.
	 */
	public SyntaxElement(final String name, final String beginThing, final String endThing, final String regex, final int regexFlags,
			final String xHTMLReplacement, final boolean isMajor) {
		this.name = name;
		this.beginThing = beginThing;
		this.endThing = endThing;
		this.regex = Pattern.compile(regex.replace(begin, DataFormatter.makeRegexSafe(beginThing)).replace(end, DataFormatter.makeRegexSafe(endThing)), regexFlags);
		this.xHTMLReplacement = xHTMLReplacement;
		this.isMajor = isMajor;
	}
}
