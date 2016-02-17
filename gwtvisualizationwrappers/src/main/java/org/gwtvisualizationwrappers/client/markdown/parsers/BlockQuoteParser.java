package org.gwtvisualizationwrappers.client.markdown.parsers;
import java.util.List;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;
import org.gwtvisualizationwrappers.client.markdown.utils.ServerMarkdownUtils;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class BlockQuoteParser extends BasicMarkdownElementParser {
	RegExp p1 = RegExp.compile(MarkdownRegExConstants.BLOCK_QUOTE_REGEX);
	RegExp p2 = RegExp.compile(MarkdownRegExConstants.FENCE_CODE_BLOCK_REGEX);
	boolean inBlockQuote;
	
	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		inBlockQuote = false;
	}

	@Override
	public void processLine(MarkdownElements line) {
		MatchResult m = p1.exec(line.getMarkdown());
		
		if (m != null) {
			if (!inBlockQuote) {
				//starting block quote
				inBlockQuote = true;
				line.prependElement(ServerMarkdownUtils.START_BLOCKQUOTE_TAG);
			}
			//modify the markdown and preserve leading space to determine depth of list items
			//do not preserve any space following ">" if this is a code block fence
			StringBuilder sb = new StringBuilder();
			if(!p2.test(line.getMarkdown())) {
				sb.append(m.getGroup(3));
			}
			sb.append(m.getGroup(4));
			line.updateMarkdown(sb.toString());
		}
		else {
			if (inBlockQuote){
				inBlockQuote = false;
				//finish block quote
				line.prependElement(ServerMarkdownUtils.END_BLOCKQUOTE_TAG);
			}
			//no need to modify the markdown
		}
	}
	
	@Override
	public boolean isInMarkdownElement() {
		return inBlockQuote;
	}
	
	@Override
	public boolean isInputSingleLine() {
		return false;
	}
}
