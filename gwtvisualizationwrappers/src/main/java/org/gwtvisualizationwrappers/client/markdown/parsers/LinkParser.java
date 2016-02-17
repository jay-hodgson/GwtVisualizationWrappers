package org.gwtvisualizationwrappers.client.markdown.parsers;
import java.util.List;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;
import org.gwtvisualizationwrappers.client.markdown.constants.WidgetConstants;
import org.gwtvisualizationwrappers.client.markdown.utils.ServerMarkdownUtils;

import com.google.gwt.dom.client.Document;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class LinkParser extends BasicMarkdownElementParser  {
	RegExp p1 = RegExp.compile(MarkdownRegExConstants.LINK_REGEX, MarkdownRegExConstants.GLOBAL);
	RegExp protocol = RegExp.compile(MarkdownRegExConstants.LINK_URL_PROTOCOL);
	RegExp synapseIdPattern = RegExp.compile(MarkdownRegExConstants.LINK_SYNAPSE);
	
	MarkdownExtractor extractor;
	MarkdownElementParser widgetParser;
	private List<MarkdownElementParser> simpleParsers;
	
	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		extractor = new MarkdownExtractor();
		for(int i = 0; i < simpleParsers.size(); i++) {
			MarkdownElementParser parser = simpleParsers.get(i);
			if(parser instanceof SynapseMarkdownWidgetParser) {
				widgetParser = parser;
				 break;
			} 
		}
		this.simpleParsers = simpleParsers;
	}
	
	private String getCurrentDivID() {
		return WidgetConstants.DIV_ID_LINK_PREFIX + extractor.getCurrentContainerId() + suffix;
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
			
			String text = m.getGroup(2);
			String url = m.getGroup(3);
			StringBuilder updated = new StringBuilder();
			//If the "url" targets a bookmarked element in the page, replace it with widget syntax 
			//for the renderer to attach a handler
			String testUrl = url.toLowerCase();
			//is this a synapse id?
			MatchResult synapseIdMatcher = synapseIdPattern.exec(testUrl);
			if(synapseIdMatcher != null) {
				//is there a version defined?
				String versionString = "";
				if (synapseIdMatcher.getGroup(2) != null && synapseIdMatcher.getGroup(2).trim().length() > 0) {
					versionString = "/version/" + synapseIdMatcher.getGroup(2);
				}
				url = "#!Synapse:" + synapseIdMatcher.getGroup(1) + versionString;
			} else {
				//Check for incomplete urls (i.e. urls starting without http/ftp/file/#)
				MatchResult protocolMatcher = protocol.exec(testUrl);
				if(protocolMatcher != null) {
					if (!testUrl.startsWith("#"))
						url = WidgetConstants.URL_PROTOCOL + url; 
					else {
						//starts with '#'.  if does not include bang, add it here
						if(testUrl.length() > 1 && testUrl.charAt(1) != '!')
							url = "#!" + url.substring(1);
					}
				}
			}
			
			StringBuilder html = new StringBuilder();
			html.append(ServerMarkdownUtils.getStartLink(getClientHostString(), url));
			html.append(url + "\">");
			String processedText = runSimpleParsers(text, simpleParsers);
			html.append(processedText + ServerMarkdownUtils.END_LINK);
			extractor.putContainerIdToContent(getCurrentDivID(), html.toString());
			
			updated.append(extractor.getContainerElementStart() + getCurrentDivID());
			updated.append("\">" + extractor.getContainerElementEnd());
			
			sb.append(updated.toString());
			m = p1.exec(line.getMarkdown());
		}
		sb.append(md.substring(index));
		
		line.updateMarkdown(sb.toString());
		//Check for new bookmark widget
		widgetParser.processLine(line);
	}
	
	
	@Override
	public void completeParse(Document doc) {
		ServerMarkdownUtils.insertExtractedContentToMarkdown(extractor, doc, true);
		//and extract any widgets that may have been in the link text
		widgetParser.completeParse(doc);
	}
}
