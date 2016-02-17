package org.gwtvisualizationwrappers.client.markdown.parsers;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class HeadingParser extends BasicMarkdownElementParser  {
	RegExp p1 = RegExp.compile(MarkdownRegExConstants.HEADING_REGEX);
	boolean matchedLine;
	
	@Override
	public void processLine(MarkdownElements line) {
		MatchResult m = p1.exec(line.getMarkdown());
		if (m != null) {
			//looks like a heading
			String prefix = m.getGroup(1);
			String hashes = m.getGroup(2);
            String headingText = m.getGroup(3);
            int level = hashes.length();
            String tag = "h" + level;
            line.updateMarkdown(prefix + "<" + tag + ">" + headingText + "</" + tag + ">");
		}
	}
	
	@Override
	public boolean isBlockElement() {
		return true;
	}
	@Override
	public boolean isInMarkdownElement() {
		return matchedLine;
	}
}
