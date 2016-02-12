package org.gwtvisualizationwrappers.client.markdown.parsers;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;

import com.google.gwt.regexp.shared.RegExp;

public class EscapedVerticalLineParser extends BasicMarkdownElementParser {
	RegExp p = RegExp.compile(MarkdownRegExConstants.VERTICAL_LINE_ESCAPED_REGEX);
	@Override
	public void processLine(MarkdownElements line) {
		line.updateMarkdown(p.replace(line.getMarkdown(), "&#124;"));
	}
}
