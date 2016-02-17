package org.gwtvisualizationwrappers.client.markdown.parsers;
import java.util.List;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;
import org.gwtvisualizationwrappers.client.markdown.constants.WidgetConstants;
import org.gwtvisualizationwrappers.client.markdown.utils.ServerMarkdownUtils;

import com.google.gwt.dom.client.Document;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class DoiAutoLinkParser extends BasicMarkdownElementParser {
	RegExp p = RegExp.compile(MarkdownRegExConstants.LINK_DOI, MarkdownRegExConstants.GLOBAL);
	//SWC-1883: need to protect the doi output
	MarkdownExtractor extractor;
	
	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		extractor = new MarkdownExtractor();
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
			String updated = "<a target=\"_blank\" class=\"link\" href=\"http://dx.doi.org/" + m.getGroup(1) + "\">" + m.getGroup(0) +"</a>";
			extractor.putContainerIdToContent(getCurrentDivID(), updated);
			String containerElement = extractor.getNewElementStart(getCurrentDivID()) + extractor.getContainerElementEnd();
			sb.append(containerElement);
			m = p.exec(line.getMarkdown());
		}
		sb.append(md.substring(index));
		
		line.updateMarkdown(sb.toString());
	}
	
	private String getCurrentDivID() {
		return WidgetConstants.DIV_ID_DOI_PREFIX + extractor.getCurrentContainerId() + suffix;
	}

	@Override
	public void completeParse(Document doc) {
		ServerMarkdownUtils.insertExtractedContentToMarkdown(extractor, doc, true);
	}

}
