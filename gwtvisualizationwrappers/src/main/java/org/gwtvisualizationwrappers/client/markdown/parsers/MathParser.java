package org.gwtvisualizationwrappers.client.markdown.parsers;
import java.util.List;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;
import org.gwtvisualizationwrappers.client.markdown.constants.WidgetConstants;
import org.gwtvisualizationwrappers.client.markdown.utils.ServerMarkdownUtils;

import com.google.gwt.dom.client.Document;

public class MathParser extends BasicMarkdownElementParser  {
	Pattern p1 = Pattern.compile(MarkdownRegExConstants.FENCE_MATH_BLOCK_REGEX);
	Pattern p2 = Pattern.compile(MarkdownRegExConstants.MATH_SPAN_REGEX);
	
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
		Matcher m;
		//math block
		m = p1.matcher(line.getMarkdown());
		if (m.matches()) {
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
			line.updateMarkdown(m.group(1));
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
		Matcher m = p2.matcher(line.getMarkdown());
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			//leave containers to filled in on completeParse()
			extractor.putContainerIdToContent(getCurrentDivID(), m.group(2));
			
			String containerElement = extractor.getNewElementStart(getCurrentDivID()) + extractor.getContainerElementEnd();
			m.appendReplacement(sb, containerElement);
		}
		m.appendTail(sb);
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
