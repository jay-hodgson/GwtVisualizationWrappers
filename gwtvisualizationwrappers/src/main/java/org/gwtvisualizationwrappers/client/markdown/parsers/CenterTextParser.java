package org.gwtvisualizationwrappers.client.markdown.parsers;
import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;

import com.google.gwt.regexp.shared.RegExp;

public class CenterTextParser extends BasicMarkdownElementParser {
	RegExp p = RegExp.compile(MarkdownRegExConstants.CENTER_TEXT_REGEX);
	
	@Override
	public void processLine(MarkdownElements line) {
		line.updateMarkdown(p.replace(line.getMarkdown(), "<div class=\"text-align-center\">$2</div>"));
	}
}
