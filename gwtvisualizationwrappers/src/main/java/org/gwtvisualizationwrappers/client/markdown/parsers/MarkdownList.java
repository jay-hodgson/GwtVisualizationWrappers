package org.gwtvisualizationwrappers.client.markdown.parsers;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;

import com.google.gwt.regexp.shared.RegExp;

public abstract class MarkdownList {
	private int depth;
	private boolean firstItemCreated;
	private boolean inCodeBlock;
	private RegExp p = RegExp.compile(MarkdownRegExConstants.HTML_FENCE_CODE_BLOCK_REGEX);
	
	public MarkdownList(int depth) {
		super();
		this.depth = depth;
		this.firstItemCreated = false;
		this.inCodeBlock = false;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public abstract String getStartListHtml();
	public void addListItemHtml(MarkdownElements line, String item) {
		//Close the previous list item if it exists
		closeOpenListItems(line);
		//Start a list item
		line.prependElement("<li>");
		line.prependElement("<p>");
		line.updateMarkdown(item);
		line.appendElement("</p>");
	}
	
	public void addExtraElementHtml(MarkdownElements line, String item) {
		boolean isMatch = p.test(line.getHtml());
		//Add other elements under a list item
		if(isMatch) {
			//If this is a code block, do not modify and update flag
			line.updateMarkdown(item);
			inCodeBlock = !inCodeBlock;
		} else {
			if(!inCodeBlock) {
				line.prependElement("<p>");
				line.updateMarkdown(item);
				line.appendElement("</p>");
			} else {
				//don't modify lines of code by wrapping it with paragraphs
				line.updateMarkdown(item);
			}
		}
	}
	
	public void closeOpenListItems(MarkdownElements line) {
		if(firstItemCreated) {
			line.prependElement("</li>");
		} else {
			//This is the first list item/no open list items exist
			firstItemCreated = true;
		}	
	}
	public abstract String getEndListHtml();
}
