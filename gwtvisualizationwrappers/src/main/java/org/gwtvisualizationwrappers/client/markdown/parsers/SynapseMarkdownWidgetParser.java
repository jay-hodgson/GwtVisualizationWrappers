package org.gwtvisualizationwrappers.client.markdown.parsers;

import java.util.List;
import java.util.Set;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;
import org.gwtvisualizationwrappers.client.markdown.constants.WidgetConstants;
import org.gwtvisualizationwrappers.client.markdown.utils.ServerMarkdownUtils;
import org.gwtvisualizationwrappers.client.markdown.utils.SharedMarkdownUtils;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class SynapseMarkdownWidgetParser extends BasicMarkdownElementParser {
	RegExp p = RegExp.compile(MarkdownRegExConstants.SYNAPSE_MARKDOWN_WIDGET_REGEX, MarkdownRegExConstants.CASE_INSENSITIVE + MarkdownRegExConstants.GLOBAL);
	MarkdownExtractor extractor;
	
	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		extractor = new MarkdownExtractor();
	}
	
	private String getCurrentDivID() {
		return WidgetConstants.DIV_ID_WIDGET_SYNTAX_PREFIX + extractor.getCurrentContainerId() + suffix;
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
			StringBuilder html = new StringBuilder();
			html.append(m.getGroup(2));
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
		Set<String> ids = extractor.getContainerIds();
		//For each widget syntax found, wrap with appropriate widget containers for renderers
		for(String key: ids) {
			boolean inlineWidget = false;
			Element el = doc.getElementById(key);
			if(el != null) {
				String content = extractor.getContent(key);
				int syntaxPrefixLen = WidgetConstants.DIV_ID_WIDGET_SYNTAX_PREFIX.length();
				//Extract just the id + suffix
				String id = key.substring(syntaxPrefixLen);
				String widgetHtml = SharedMarkdownUtils.getWidgetHTML(id, content);
				if(content.contains(WidgetConstants.INLINE_WIDGET_KEY)) {
					inlineWidget = true;
				}
				
				StringBuilder newHtml = new StringBuilder();
				if(inlineWidget) {
					newHtml.append("<span>"+widgetHtml+"</span>");
				} else {
					newHtml.append("<div>"+widgetHtml+"</div>");
				}
				
				//Surround with widget holder and appropriate container
				el.setInnerHTML(newHtml.toString());
			}	
		}
	}

}
