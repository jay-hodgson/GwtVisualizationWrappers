package org.gwtvisualizationwrappers.client.markdown.parsers;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;

import com.google.gwt.regexp.shared.RegExp;

public class SuperscriptParser extends BasicMarkdownElementParser {
	RegExp p = RegExp.compile(MarkdownRegExConstants.SUPERSCRIPT_REGEX);
	@Override
	public void processLine(MarkdownElements line) {
		line.updateMarkdown(p.replace(line.getMarkdown(), "<sup>$2</sup>"));
	}
}
