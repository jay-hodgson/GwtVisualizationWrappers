package org.gwtvisualizationwrappers.client.markdown.parsers;
import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;

import com.google.gwt.regexp.shared.RegExp;

public class BookmarkTargetParser extends BasicMarkdownElementParser {
	
	RegExp p = RegExp.compile(MarkdownRegExConstants.BOOKMARK_TARGET_REGEX);
	
	@Override
	public void processLine(MarkdownElements line) {
		line.updateMarkdown(p.replace(line.getMarkdown(), "<p class=\"inlineWidgetContainer\" id=\"$1\"></p>"));
	}
}
