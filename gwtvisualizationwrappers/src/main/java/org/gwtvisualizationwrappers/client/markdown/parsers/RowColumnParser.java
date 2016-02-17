package org.gwtvisualizationwrappers.client.markdown.parsers;
import java.util.List;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;
import org.gwtvisualizationwrappers.client.markdown.utils.ServerMarkdownUtils;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class RowColumnParser extends BasicMarkdownElementParser {
	RegExp rowPattern = RegExp.compile(MarkdownRegExConstants.ROW_REGEX, MarkdownRegExConstants.GLOBAL);
	RegExp columnPattern = RegExp.compile(MarkdownRegExConstants.COLUMN_REGEX, MarkdownRegExConstants.GLOBAL);
	private boolean isInRow;
	private boolean isInColumn;
	
	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		isInRow = false;
		isInColumn = false;
	}
	
	@Override
	public void processLine(MarkdownElements line) {
		rowPattern.setLastIndex(0);
		String md = line.getMarkdown();
		MatchResult m = rowPattern.exec(md);
		StringBuffer sb = new StringBuffer();
		
		int index = 0;
		while(m != null) {
			sb.append(md.substring(index, m.getIndex()));
			index = ServerMarkdownUtils.indexAfterMatch(m);
			//found row, add html and flip isInRow
			if (!isInRow) {
				sb.append("<div class=\"row\">");	
			} else {
				sb.append("</div>");
			}
			isInRow = !isInRow;
			m = rowPattern.exec(md);
		}
		sb.append(md.substring(index));
		line.updateMarkdown(sb.toString());
		
		
		//detect columns
		columnPattern.setLastIndex(0);
		m = columnPattern.exec(md);
		sb = new StringBuffer();
		index = 0;
		while(m != null) {
			sb.append(md.substring(index, m.getIndex()));
			index = ServerMarkdownUtils.indexAfterMatch(m);
			//found column, add html and flip isInColumn
			
			String parameters = m.getGroup(1);
			StringBuilder cssClassString = new StringBuilder();
			if (parameters.trim().length() > 0) {
				//alternate between key and value
				boolean isKey = true;
				
				//split on whitespace or '='
				String[] a = parameters.split("[\\s=]");
				for (int i = 0; i < a.length; i++) {
					//recognized key?
					String v = a[i];
					if (v.trim().length() > 0) {
						if (isKey) {
							//key
							if (v.equalsIgnoreCase("width")) {
								cssClassString.append(" col-sm-");
							} else if (v.equalsIgnoreCase("offset")) {
								cssClassString.append(" col-sm-offset-");
							}
							//next token will be the value
							isKey = false;
						} else {
							//value
							cssClassString.append(v + " ");
							//next token will be a key
							isKey = true;
						}
					}
				}
			}
			
			if (!isInColumn) {
				sb.append("<div class=\"" + cssClassString.toString() + "\">");	
			} else {
				sb.append("</div>");
			}
			isInColumn = !isInColumn;
			m = columnPattern.exec(md);
		}
		sb.append(md.substring(index));
		
		if (isInRow && isInColumn) {
			//if we are in the row, do not output an html line break unless we're in a column too
			sb.append(ServerMarkdownUtils.HTML_LINE_BREAK);
		}
		line.updateMarkdown(sb.toString());
	}
	
	@Override
	public boolean isBlockElement() {
		//we handle newlines
		return true;
	}
	
	@Override
	public boolean isInMarkdownElement() {
		return isInRow;
	}

}
