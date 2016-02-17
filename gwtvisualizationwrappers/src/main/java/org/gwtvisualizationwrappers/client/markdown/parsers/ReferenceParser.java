package org.gwtvisualizationwrappers.client.markdown.parsers;

import java.util.ArrayList;
import java.util.List;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;
import org.gwtvisualizationwrappers.client.markdown.constants.WidgetConstants;
import org.gwtvisualizationwrappers.client.markdown.utils.ServerMarkdownUtils;
import org.gwtvisualizationwrappers.client.markdown.utils.WidgetEncodingUtil;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;


public class ReferenceParser extends BasicMarkdownElementParser {
	RegExp p1 = RegExp.compile(MarkdownRegExConstants.REFERENCE_REGEX, MarkdownRegExConstants.GLOBAL);
	ArrayList<String> footnotes;
	List<MarkdownElementParser> parsersOnCompletion;
	int footnoteNumber;
	
	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		footnotes = new ArrayList<String>();
		footnoteNumber = 1;
		parsersOnCompletion = simpleParsers;
	}

	@Override
	public void processLine(MarkdownElements line) {
		p1.setLastIndex(0);
		String input = line.getMarkdown();
		MatchResult m = p1.exec(input);
		StringBuffer sb = new StringBuffer();
		int index = 0;
		while(m != null) {
			sb.append(input.substring(index, m.getIndex()));
			index = ServerMarkdownUtils.indexAfterMatch(m);
			//Expression has 4 groupings (2 parameter/value pairs.)
			//Store the reference text
			for(int i = 1; i < 4; i += 2) {
				String param = m.getGroup(i);
				if(param.contains("text")) {
					footnotes.add(m.getGroup(i+1));
				}
			}
			
			/*
			 * Insert:
			 * 1) Bookmark target so that footnotes can link back to the reference
			 * 2) add a footnoteId param to the original syntax to tell the renderer which footnote to link to
			 */
			String referenceId = WidgetConstants.REFERENCE_ID_WIDGET_PREFIX + footnoteNumber;
			String footnoteParameter = WidgetConstants.REFERENCE_FOOTNOTE_KEY + "=" + footnoteNumber;
			
			String updated = "<span id=\"" + referenceId + "\">&nbsp;</span>" + m.getGroup(0) + "&amp;" + footnoteParameter + "}";
			sb.append(updated);
			footnoteNumber++;
			m = p1.exec(input);
		}
		sb.append(input.substring(index));
		line.updateMarkdown(sb.toString());
	}
	
	@Override
	public void completeParse(StringBuilder html) {
		if (footnotes.size() > 0)
			html.append("<hr>");
		StringBuilder footnoteMarkdown = new StringBuilder();
		for(int i = 0; i < footnotes.size(); i++) {
			String footnoteText = WidgetEncodingUtil.decodeValue(footnotes.get(i));
			String targetReferenceId = WidgetConstants.REFERENCE_ID_WIDGET_PREFIX + (i + 1);
			String footnoteId = WidgetConstants.FOOTNOTE_ID_WIDGET_PREFIX + (i + 1);
			
			//Insert bookmark to link back to the reference.
			//SWC-2453: instead of relying on the link parser to insert the bookmark widget, do it here.
			footnoteMarkdown.append(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.BOOKMARK_CONTENT_TYPE + "?");
			footnoteMarkdown.append(WidgetConstants.TEXT_KEY + "=" + "[" + (i + 1) + "]&");
			footnoteMarkdown.append(WidgetConstants.INLINE_WIDGET_KEY + "=true&");
			footnoteMarkdown.append(WidgetConstants.BOOKMARK_KEY + "=" + targetReferenceId);
			footnoteMarkdown.append(WidgetConstants.WIDGET_END_MARKDOWN);			

			//Assign id to the element so that the reference can link to this footnote
			footnoteMarkdown.append("<span id=\"" + footnoteId + "\" class=\"margin-left-5\">" + footnoteText + "</span>");
			footnoteMarkdown.append("<br>");
		}
		String parsedFootnotes = runSimpleParsers(footnoteMarkdown.toString(), parsersOnCompletion);
		html.append(parsedFootnotes);
	}
}
