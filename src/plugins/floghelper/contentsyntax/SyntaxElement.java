/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.contentsyntax;

import java.util.regex.Pattern;

/**
 * Immutable object representing a syntax base element. Examples : bold text,
 * headings, ordered list, ...
 * @author Artefact2
 */
public class SyntaxElement {
	public static final String begin = "___${BEGIN}___";
	public static final String end = "___${END}___";

	public final String name;
	public final String beginThing;
	public final String endThing;
	public final Pattern regex;
	public final String xHTMLReplacement;
	/**
	 * If false, it won't be shown in the help and in the JS editbox.
	 */
	public final boolean isMajor;

	public SyntaxElement(final String name, final String beginThing, final String endThing) {
		this(name, beginThing, endThing, "", 0, "", true);
	}

	public SyntaxElement(final String name, final String beginThing, final String endThing, final String regex, final int regexFlags,
			final String xHTMLReplacement, final boolean isMajor) {
		this.name = name;
		this.beginThing = beginThing;
		this.endThing = endThing;
		this.regex = Pattern.compile(regex.replace(begin, makeRegexSafe(beginThing)).replace(end, makeRegexSafe(endThing)), regexFlags);
		this.xHTMLReplacement = xHTMLReplacement;
		this.isMajor = isMajor;
	}

	public static String makeRegexSafe(String s) {
		// FIXME that list is not exhaustive at all.
		return s.replace("\\", "\\\\").replace("[", "\\[").replace("]", "\\]")
				.replace("*", "\\*").replace(".", "\\.").replace("|", "\\|").replace("^", "\\^")
				.replace("?", "\\?").replace("(", "\\(").replace(")", "\\)").replace("+", "\\+");
	}

	public static String htmlSpecialChars(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
}
