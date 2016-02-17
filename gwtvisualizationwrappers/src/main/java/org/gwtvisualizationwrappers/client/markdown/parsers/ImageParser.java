package org.gwtvisualizationwrappers.client.markdown.parsers;
import java.util.List;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;
import org.gwtvisualizationwrappers.client.markdown.constants.WidgetConstants;
import org.gwtvisualizationwrappers.client.markdown.utils.ServerMarkdownUtils;

import com.google.gwt.dom.client.Document;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class ImageParser extends BasicMarkdownElementParser {
	RegExp p1 = RegExp.compile(MarkdownRegExConstants.IMAGE_REGEX, MarkdownRegExConstants.GLOBAL);
	MarkdownExtractor extractor;

	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		extractor = new MarkdownExtractor();
	}
	
	private String getCurrentDivID() {
		return WidgetConstants.DIV_ID_IMAGE_PREFIX + extractor.getCurrentContainerId() + suffix;
	}

	@Override
	public void processLine(MarkdownElements line) {
		p1.setLastIndex(0);
		String md = line.getMarkdown();
		MatchResult m = p1.exec(md);
		StringBuffer sb = new StringBuffer();
		int index = 0;
		while(m != null) {
			sb.append(md.substring(index, m.getIndex()));
			index = ServerMarkdownUtils.indexAfterMatch(m);
			String src = m.getGroup(2);
			String alt = m.getGroup(1);
			StringBuilder html = new StringBuilder();
			html.append("<img src=\"");
			html.append(src + "\" alt=\"");
			html.append(alt + "\" />");
			extractor.putContainerIdToContent(getCurrentDivID(), html.toString());
			String containerElement = extractor.getNewElementStart(getCurrentDivID()) + extractor.getContainerElementEnd();
			sb.append(containerElement);
			m = p1.exec(line.getMarkdown());
		}
		sb.append(md.substring(index));
		line.updateMarkdown(sb.toString());
	}

	@Override
	public void completeParse(Document doc) {
		ServerMarkdownUtils.insertExtractedContentToMarkdown(extractor, doc, true);
	}
}
