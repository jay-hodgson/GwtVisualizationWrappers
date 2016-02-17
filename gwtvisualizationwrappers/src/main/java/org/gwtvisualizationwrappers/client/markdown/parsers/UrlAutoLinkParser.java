package org.gwtvisualizationwrappers.client.markdown.parsers;

import java.util.List;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;
import org.gwtvisualizationwrappers.client.markdown.constants.WidgetConstants;
import org.gwtvisualizationwrappers.client.markdown.utils.ServerMarkdownUtils;

import com.google.gwt.dom.client.Document;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;


public class UrlAutoLinkParser extends BasicMarkdownElementParser {
	RegExp p = RegExp.compile(MarkdownRegExConstants.LINK_URL, MarkdownRegExConstants.GLOBAL);
	MarkdownExtractor extractor;

	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		extractor = new MarkdownExtractor();
	}
	
	private String getCurrentDivID() {
		return WidgetConstants.DIV_ID_AUTOLINK_PREFIX + extractor.getCurrentContainerId() + suffix;
	}
	
	@Override
	public void processLine(MarkdownElements line) {
		p.setLastIndex(0);
		String md = line.getMarkdown();
		MatchResult m = p.exec(md);
		StringBuffer sb = new StringBuffer();
		int index = 0;
		while(m != null) {
			sb.append(md.substring(index, m.getIndex()));
			index = ServerMarkdownUtils.indexAfterMatch(m);
			String url = m.getGroup(1).trim();
			StringBuilder html = new StringBuilder();
			html.append(ServerMarkdownUtils.getStartLink(getClientHostString(), url));
			html.append(url + "\">");
			html.append(url + ServerMarkdownUtils.END_LINK);
			extractor.putContainerIdToContent(getCurrentDivID(), html.toString());
			String containerElement = extractor.getNewElementStart(getCurrentDivID()) + extractor.getContainerElementEnd();
			sb.append(containerElement);
			m = p.exec(line.getMarkdown());
		}
		sb.append(md.substring(index));
		line.updateMarkdown(sb.toString());
	}

	@Override
	public void completeParse(Document doc) {
		ServerMarkdownUtils.insertExtractedContentToMarkdown(extractor, doc, true);
	}
	
}
