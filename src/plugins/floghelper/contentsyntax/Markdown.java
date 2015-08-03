package plugins.floghelper.contentsyntax;

import plugins.floghelper.FlogHelper;

import org.commonmark.html.HtmlRenderer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

/**
 * Support for Markdown / CommonMark
 */
public class Markdown extends ContentSyntax {

    public Markdown() {
        syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Bold"), "**"));
        syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Italic"), "*"));
        /* As of version 0.20 (2015-06-08) CommonMark does not support underline. */

        syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H1"), "#", ""));
        syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H2"), "##", ""));
        syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H3"), "###", ""));
        syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H4"), "####", ""));
        syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H5"),
                           "#####", ""));
        syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("H6"),
                           "######", ""));

        syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Picture"),
                           "![Alt text](key", "\"title\")"));

        syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Code"),
                           "    ", ""));
        syntaxElements.add(new SyntaxElement(FlogHelper.getBaseL10n().getString("Quote"),
                           "> ", ""));
    }

    @Override
    public String parseSomeString(String s) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(s);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }
}
