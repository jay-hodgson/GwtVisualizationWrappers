package org.gwtvisualizationwrappers.client.markdown.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gwtvisualizationwrappers.client.markdown.constants.WidgetConstants;
import org.gwtvisualizationwrappers.client.markdown.parsers.MarkdownExtractor;
import org.gwtvisualizationwrappers.client.markdown.parsers.TableParser;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.regexp.shared.MatchResult;

public class ServerMarkdownUtils {
	public static final String START_PRE_CODE = "<pre><code";
	public static final String END_PRE_CODE = "</code></pre>";
	
	public static final String START_CONTAINER = "<span id=\"";
	public static final String END_CONTAINER = "</span>";
	
	public static final String START_LINK_NEW_WINDOW = "<a class=\"link\" target=\"_blank\" href=\"";
	public static final String START_LINK_CURRENT_WINDOW = "<a class=\"link\" href=\"";
	
	public static final String END_LINK = "</a>";
	
	public static final String START_BLOCKQUOTE_TAG = "<blockquote>";
	public static final String END_BLOCKQUOTE_TAG = "</blockquote>";
	public static final String HTML_LINE_BREAK = "<br />\n";
	
	public static String getStartLink(String clientHostString, String href) {
		//default is to stay in the current window
		if (clientHostString == null || clientHostString.length() == 0 || href == null || href.length() == 0)
			return START_LINK_CURRENT_WINDOW;
		return href.toLowerCase().startsWith(clientHostString) || href.charAt(0) == '#' ?
				START_LINK_CURRENT_WINDOW : 
				START_LINK_NEW_WINDOW;
	}
	
	/**
	 * Retrieves each container specified by saved ids and inserts the associated contents into the container
	 * @param extractor
	 * @param doc
	 */
	public static void insertExtractedContentToMarkdown(MarkdownExtractor extractor, Document doc, boolean hasHtml) {
		Set<String> foundKeys = new HashSet<String>();
		for(String key: extractor.getContainerIds()) {
			Element el = doc.getElementById(key);
			if(el != null) {
				if(hasHtml) {
					el.setInnerHTML(extractor.getContent(key));
				} else {
					el.setInnerText(extractor.getContent(key));
				}
				foundKeys.add(key);
			}
		}
		//clean up the container Ids that we resolved
		extractor.removeContainerIds(foundKeys);
	}
	
	
	
//	private static long lastTime;
//	private static void reportTime(String description) {
//		long currentTime = System.currentTimeMillis();
//		System.out.println(description + ": " + (currentTime-lastTime));
//		lastTime = currentTime;
//	}

	private static int getLargestHeadingLevel(Element doc){
		int i = 0; //start at 0
		for (; i < 7; i++) {
			if (!(doc.getElementsByTagName("h"+i).getLength() == 0))
				break;
		}
		return i;
	}
	
	public static void assignIdsToHeadings(Element doc) {
		//find the biggest heading level
		int largestHeadingLevel = getLargestHeadingLevel(doc);
		Map<String, String> headingLevel2StyleName = new HashMap<String, String>();
		int indentLevel = 0;
		for (int i = largestHeadingLevel; i < 7; i++, indentLevel++) {
			headingLevel2StyleName.put("h" + i, "toc-indent"+indentLevel);
		}
		String[] headingTagNames = new String[]{"h0", "h1", "h2", "h3", "h4", "h5", "h6"};
		
		int headingIndex = 0;
		for (int j = 0; j < headingTagNames.length; j++) {
			String targetHeadingTag = headingTagNames[j];
			NodeList<Element> hTags = doc.getElementsByTagName(targetHeadingTag);
			for (int i = 0; i < hTags.getLength(); i++) {
				Element hTag = hTags.getItem(i);
				boolean skip = false;
				String text = hTag.getInnerText();
				if (text.startsWith("!")) {
					skip=true;
					hTag.setInnerText(text.substring(1));
				}
				if (!skip) {
					hTag.setAttribute("id", WidgetConstants.MARKDOWN_HEADING_ID_PREFIX+headingIndex);
					hTag.setAttribute("level", targetHeadingTag);
					hTag.setAttribute("toc-style", headingLevel2StyleName.get(targetHeadingTag));
					headingIndex++;
				}
			}
		}
	}
	

	public static void resolveAttachmentImages(Document doc, String attachmentUrl) {
		NodeList<Element> imgTags = doc.getElementsByTagName("img");
		for (int i = 0; i < imgTags.getLength(); i++) {
			Element img = imgTags.getItem(i);
			String src = img.getAttribute("src");
			if (src.startsWith(WidgetConstants.ENTITY_DESCRIPTION_ATTACHMENT_PREFIX)){
		    	String[] tokens = src.split("/");
		    	if (tokens.length > 5) {
			        String entityId = tokens[2];
				    String tokenId = tokens[4] +"/"+ tokens[5];
				    img.setAttribute("src", ServerMarkdownUtils.createAttachmentUrl(attachmentUrl, entityId, tokenId, tokenId,WidgetConstants.ENTITY_PARAM_KEY));
		    	}
			}
		}
	}

	/**
	 * Create the url to an attachment image.
	 * @param baseURl
	 * @param id
	 * @param tokenId
	 * @param fileName
	 * @return
	 */
	public static String createAttachmentUrl(String baseURl, String id, String tokenId, String fileName, String paramKey){
	        StringBuilder builder = new StringBuilder();
	        builder.append(baseURl);
	        builder.append("?"+paramKey+"=");
	        builder.append(id);
	        builder.append("&"+WidgetConstants.TOKEN_ID_PARAM_KEY+"=");
	        builder.append(tokenId);
	        builder.append("&"+WidgetConstants.WAIT_FOR_URL+"=true");
	        return builder.toString();
	}
	
	
	
	
	public static int appendNewTableHtml(StringBuilder builder, String regEx, String[] lines, int tableCount, int i) {
		builder.append(TableParser.TABLE_START_HTML+WidgetConstants.MARKDOWN_TABLE_ID_PREFIX+tableCount+"\" class=\"tablesorter\">");
		//header
		builder.append("<thead>");
		builder.append("<tr>");
		String[] cells = lines[i].split("\\|");
		for (int j = 0; j < cells.length; j++) {
			builder.append("<th>");
			builder.append(cells[j]);
			builder.append("</th>");
		}
		builder.append("</tr>");
		builder.append("</thead>");
		builder.append("<tbody>");
		i++;
		while (i < lines.length && lines[i].matches(regEx)) {
			builder.append("<tr>");
			cells = lines[i].split("\\|");
			for (int j = 0; j < cells.length; j++) {
				builder.append("<td>");
				builder.append(cells[j]);
				builder.append("</td>");
			}
			builder.append("</tr>");
			i++;
		}
		builder.append(TableParser.TABLE_END_HTML);
		
		return i;
	}
	
	public static String getSynAnchorHtml(String synId){
		return "<a class=\"link\" href=\"#!Synapse:" + synId +"\">" + synId + "</a>";
	}
	
	public static String getDoiLink(String fullDoi, String doiName){
		return "<a target=\"_blank\" class=\"link\" href=\"http://dx.doi.org/" +
				doiName + "\">" + fullDoi +"</a>";
	}
	
	public static String getUrlHtml(String url){
		return "<a target=\"_blank\" class=\"link\" href=\"" + url.trim() + "\">" + url+ "</a>";
	}

	public static int indexAfterMatch(MatchResult m) {
		if (m == null) {
			return -1;
		}
		return  m.getIndex() + m.getGroup(0).length();
	}
	public static final String DEFAULT_CODE_CSS_CLASS = "no-highlight";
}
