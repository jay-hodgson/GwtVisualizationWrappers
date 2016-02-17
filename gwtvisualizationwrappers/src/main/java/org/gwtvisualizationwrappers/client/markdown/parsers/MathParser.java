package org.gwtvisualizationwrappers.client.markdown.parsers;
import java.util.List;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;
import org.gwtvisualizationwrappers.client.markdown.constants.WidgetConstants;
import org.gwtvisualizationwrappers.client.markdown.utils.ServerMarkdownUtils;

import com.google.gwt.dom.client.Document;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class MathParser extends BasicMarkdownElementParser  {
	RegExp p1 = RegExp.compile(MarkdownRegExConstants.FENCE_MATH_BLOCK_REGEX);
	RegExp p2 = RegExp.compile(MarkdownRegExConstants.MATH_SPAN_REGEX, MarkdownRegExConstants.GLOBAL);
	
	MarkdownExtractor extractor;
	boolean isInMathBlock, isFirstMathLine;
	
	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		isInMathBlock = false;
		isFirstMathLine = false;
		extractor = new MarkdownExtractor();
	}

	private String getCurrentDivID() {
		return WidgetConstants.DIV_ID_MATHJAX_PREFIX + extractor.getCurrentContainerId() + suffix;
	}
	
	@Override
	public void processLine(MarkdownElements line) {
		MatchResult m;
		//math block
		m = p1.exec(line.getMarkdown());
		if (m != null) {
			if (!isInMathBlock) {
				//starting math block
				isInMathBlock = true;
				isFirstMathLine = true;
				line.prependElement(extractor.getNewElementStart(getCurrentDivID()));
			}
			else {
				//ending math block
				line.appendElement(extractor.getContainerElementEnd());
				isInMathBlock = false;
			}
			//remove all fenced blocks from the markdown, just set to the prefix group
			line.updateMarkdown(m.getGroup(1));
		}
		else {
			if (isInMathBlock) {
				if (!isFirstMathLine) {
					line.prependElement("\n");	
				}
			}
			else {
				//not currently in a math block, and this is not the start/end of a math block
				//check for math span
				processMathSpan(line);		
			}
			if (isFirstMathLine)
				isFirstMathLine = false;
		}		
	}

	private void processMathSpan(MarkdownElements line) {
		p2.setLastIndex(0);
		String md = line.getMarkdown();
		MatchResult m = p2.exec(md);
		StringBuffer sb = new StringBuffer();
		int index = 0;
		while(m != null) {
			sb.append(md.substring(index, m.getIndex()));
			index = ServerMarkdownUtils.indexAfterMatch(m);
			extractor.putContainerIdToContent(getCurrentDivID(), m.getGroup(2));
			String containerElement = extractor.getNewElementStart(getCurrentDivID()) + extractor.getContainerElementEnd();
			sb.append(containerElement);
			m = p1.exec(line.getMarkdown());
		}
		sb.append(md.substring(index));
		line.updateMarkdown(sb.toString());
	}
	
	/**
	 * Fill in the stored equations into the containers that we made during parse
	 */
	@Override
	public void completeParse(Document doc) {
		ServerMarkdownUtils.insertExtractedContentToMarkdown(extractor, doc, false);
	}

	@Override
	public boolean isInMarkdownElement() {
		return isInMathBlock;
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
