package org.gwtvisualizationwrappers.client.markdown.parsers;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;

import com.google.gwt.regexp.shared.RegExp;

public class SynapseAutoLinkParser extends BasicMarkdownElementParser {
	RegExp p = RegExp.compile(MarkdownRegExConstants.LINK_SYNAPSE);
	
	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p.matcher(line.getMarkdown());
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			//is there a version defined?
			String versionString = "";
			if (m.group(2) != null && m.group(2).trim().length() > 0) {
				versionString = "/version/" + m.group(2);
			}
			String updated = "<a class=\"link\" href=\"#!Synapse:" + m.group(1) + versionString + "\">" + m.group(0) + "</a>";
			m.appendReplacement(sb, updated);
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}

}
