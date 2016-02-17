package org.gwtvisualizationwrappers.client.markdown.parsers;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;
import org.gwtvisualizationwrappers.client.markdown.utils.ServerMarkdownUtils;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class SynapseAutoLinkParser extends BasicMarkdownElementParser {
	RegExp p = RegExp.compile(MarkdownRegExConstants.LINK_SYNAPSE, MarkdownRegExConstants.GLOBAL);
	
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
			
			//is there a version defined?
			String versionString = "";
			if (m.getGroup(2) != null && m.getGroup(2).trim().length() > 0) {
				versionString = "/version/" + m.getGroup(2);
			}
			String updated = "<a class=\"link\" href=\"#!Synapse:" + m.getGroup(1) + versionString + "\">" + m.getGroup(0) + "</a>";
			
			sb.append(updated);
			m = p.exec(md);
		}
		sb.append(md.substring(index));
		line.updateMarkdown(sb.toString());
	}

}
