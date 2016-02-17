package org.gwtvisualizationwrappers.client.markdown.parsers;
import java.util.List;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;
import org.gwtvisualizationwrappers.client.markdown.utils.ServerMarkdownUtils;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;


public class CodeParser extends BasicMarkdownElementParser  {
	RegExp p = RegExp.compile(MarkdownRegExConstants.FENCE_CODE_BLOCK_REGEX);
	boolean isInCodeBlock, isFirstCodeLine;
	
	
	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		isInCodeBlock = false;
		isFirstCodeLine = false;
	}

	@Override
	public void processLine(MarkdownElements line) {
		MatchResult m = p.exec(line.getMarkdown());
		if (m != null) {
			if (!isInCodeBlock) {
				//starting code block
				isInCodeBlock = true;
				isFirstCodeLine = true;
				StringBuilder sb = new StringBuilder();
				sb.append(ServerMarkdownUtils.START_PRE_CODE);
				String codeCssClass = null;
				if (m.getGroupCount() == 2)
					codeCssClass = m.getGroup(2).toLowerCase();
				if (codeCssClass == null || codeCssClass.trim().length() == 0) {
					codeCssClass = ServerMarkdownUtils.DEFAULT_CODE_CSS_CLASS;
				}
				sb.append(" class=\""+codeCssClass+"\"");
				sb.append(">");
				line.prependElement(sb.toString());
			}
			else {
				//ending code block
				line.appendElement(ServerMarkdownUtils.END_PRE_CODE);
				isInCodeBlock = false;
			}
			//remove all fenced code blocks from the markdown, just set to the prefix group
			line.updateMarkdown(m.getGroup(1));
		}
		else {
			if (isInCodeBlock && !isFirstCodeLine)
				line.prependElement("\n");
			
			if (isFirstCodeLine)
				isFirstCodeLine = false;
		}
	}

	@Override
	public boolean isInMarkdownElement() {
		return isInCodeBlock;
	}
	
	@Override
	public boolean isBlockElement() {
		return true;
	}
	
	@Override
	public boolean isInputSingleLine() {
		return false;
	}

}
