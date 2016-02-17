package org.gwtvisualizationwrappers.client.markdown.parsers;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;


public class HorizontalLineParser extends BasicMarkdownElementParser  {
	RegExp p1 = RegExp.compile(MarkdownRegExConstants.HR_REGEX1);
	RegExp p2 = RegExp.compile(MarkdownRegExConstants.HR_REGEX2);

	@Override
	public void processLine(MarkdownElements line) {
		String testLine = line.getMarkdown().replaceAll(" ", "");
		MatchResult m1 = p1.exec(testLine);
		MatchResult m2 = p2.exec(testLine);
		
		boolean isHr = m1 != null || m2 != null;
		if (isHr) {
			//output hr
			line.updateMarkdown("<hr>");
		}
	}
}
