package org.gwtvisualizationwrappers.client.markdown;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gwtvisualizationwrappers.client.markdown.constants.MarkdownRegExConstants;
import org.gwtvisualizationwrappers.client.markdown.parsers.BlockQuoteParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.BoldParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.BookmarkTargetParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.CenterTextParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.CodeParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.CodeSpanParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.DoiAutoLinkParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.EscapedBacktickParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.EscapedDashParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.EscapedUnderscoreParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.EscapedVerticalLineParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.HeadingParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.HorizontalLineParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.ImageParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.ItalicsParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.LinkParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.ListParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.MarkdownElementParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.MarkdownElements;
import org.gwtvisualizationwrappers.client.markdown.parsers.MathParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.ReferenceParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.RowColumnParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.StrikeoutParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.SubscriptParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.SuperscriptParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.SynapseAutoLinkParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.SynapseMarkdownWidgetParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.TableParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.TildeParser;
import org.gwtvisualizationwrappers.client.markdown.parsers.UrlAutoLinkParser;
import org.gwtvisualizationwrappers.client.markdown.utils.ServerMarkdownUtils;

import com.google.gwt.dom.client.Document;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;


public class SynapseMarkdownProcessor {
	private static SynapseMarkdownProcessor singleton = null;
	private List<MarkdownElementParser> allElementParsers = new ArrayList<MarkdownElementParser>();
	private RegExp blockquotePatternProtector, gtProtector, ltProtector;
	private CodeParser codeParser;
	private MathParser mathParser;
	public static SynapseMarkdownProcessor getInstance() {
		if (singleton == null) {
			singleton = new SynapseMarkdownProcessor();
		}
		return singleton;
	}
	
	private SynapseMarkdownProcessor() {
		init();
	}
	
	private void init() {
		//protect widget syntax
		allElementParsers.add(new ReferenceParser());
		allElementParsers.add(new BookmarkTargetParser());
		allElementParsers.add(new SynapseMarkdownWidgetParser());
		
		//parsers that handle escaping
		allElementParsers.add(new TildeParser());
		allElementParsers.add(new EscapedUnderscoreParser());
		allElementParsers.add(new EscapedBacktickParser());
		allElementParsers.add(new EscapedVerticalLineParser());
		allElementParsers.add(new EscapedDashParser());
		//other parsers should not affect code spans
		allElementParsers.add(new CodeSpanParser());
		//parsers protecting urls go before other simple parsers
		allElementParsers.add(new ImageParser());
		allElementParsers.add(new DoiAutoLinkParser());
		allElementParsers.add(new LinkParser());
		allElementParsers.add(new UrlAutoLinkParser());
		
		//initialize other markdown element parsers
		allElementParsers.add(new BlockQuoteParser());
		allElementParsers.add(new BoldParser());	
		codeParser = new CodeParser();
		allElementParsers.add(codeParser);
		mathParser = new MathParser();
		allElementParsers.add(mathParser);
		allElementParsers.add(new HeadingParser());
		allElementParsers.add(new HorizontalLineParser());
		allElementParsers.add(new ItalicsParser());
		allElementParsers.add(new ListParser());
		allElementParsers.add(new StrikeoutParser());
		allElementParsers.add(new SubscriptParser());
		allElementParsers.add(new SuperscriptParser());
		allElementParsers.add(new CenterTextParser());
		allElementParsers.add(new SynapseAutoLinkParser());
		allElementParsers.add(new TableParser());
		allElementParsers.add(new RowColumnParser());
		blockquotePatternProtector = RegExp.compile("^&gt;", MarkdownRegExConstants.MULTILINE + MarkdownRegExConstants.GLOBAL);
		gtProtector = RegExp.compile(">", MarkdownRegExConstants.MULTILINE + MarkdownRegExConstants.GLOBAL);
		ltProtector = RegExp.compile("<", MarkdownRegExConstants.MULTILINE + MarkdownRegExConstants.GLOBAL);
	}
	
	/**
	 * This converts the given markdown to html using the given markdown processor.
	 * It also post processes the output html, including:
	 * *sending all links to a new window.
	 * *applying the markdown css classname to entities supported by the markdown.
	 * *auto detects Synapse IDs (and creates links out of them)
	 * *auto detects generic urls (and creates links out of them)
	 * *resolve Widgets!
	 * @param panel
	 * @throws IOException 
	 */
	public Widget markdown2Html(String markdown, String suffix, String clientHostString) throws IOException {
		String originalMarkdown = markdown;
		if (markdown == null || markdown.equals("")) return new HTML();
		markdown = gtProtector.replace(markdown, "&gt;");
		markdown = ltProtector.replace(markdown, "&lt;");
		markdown = SafeHtmlUtils.htmlEscapeAllowEntities(markdown);
		markdown = blockquotePatternProtector.replace(markdown, ">");
		
		String html = processMarkdown(markdown, allElementParsers, suffix, clientHostString);
		if (html == null) {
			//if the markdown processor fails to convert the md to html (will return null in this case), return the raw markdown instead. (as ugly as it might be, it's better than no information).
			return new HTML(SafeHtmlUtils.htmlEscape(originalMarkdown)); 
		}
		//URLs are automatically resolved from the markdown processor
		return postProcessHtml(html);
	}
	
	public String processMarkdown(String markdown, List<MarkdownElementParser> parsers, String suffix, String clientHostString) {
		//go through the document once, and apply all markdown parsers to it
		StringBuilder output = new StringBuilder();
		if (suffix == null) {
			suffix = "";
		}
		
		//these are the parsers that only take a single line as input (element does not span across lines)
		List<MarkdownElementParser> simpleParsers = new ArrayList<MarkdownElementParser>();
		
		//these are the processors that report they are in the middle of a multiline element
		List<MarkdownElementParser> activeComplexParsers = new ArrayList<MarkdownElementParser>();
		//the rest of the multiline processors not currently in the middle of an element
		List<MarkdownElementParser> inactiveComplexParsers = new ArrayList<MarkdownElementParser>();
		
		//initialize all processors either in the simple list, or in the inactive list
		for (MarkdownElementParser parser : parsers) {
			if (parser.isInputSingleLine())
				simpleParsers.add(parser);
			else
				inactiveComplexParsers.add(parser);
		}
		
		//reset all of the parsers
		String lowerClientHostString = clientHostString == null ? "" : clientHostString.toLowerCase();
		for (MarkdownElementParser parser : parsers) {
			parser.reset(simpleParsers);
			parser.setSuffix(suffix);
			parser.setClientHostString(lowerClientHostString);
		}
		
		List<String> allLines = new ArrayList<String>();
		for (String line : markdown.split("\n")) {
			allLines.add(line);
		}
		allLines.add("");
		for (String line : allLines) {
			MarkdownElements elements = new MarkdownElements(line);
			//do parsers we're currently in the middle of
			for (MarkdownElementParser parser : activeComplexParsers) {
				parser.processLine(elements);
			}
			
			//only give the option to start new multiline element (complex parser) or process simple elements if we're not in a code block (or a math block)
			if (!codeParser.isInMarkdownElement() && !mathParser.isInMarkdownElement()){
				//then the inactive multiline parsers
				for (MarkdownElementParser parser : inactiveComplexParsers) {
					parser.processLine(elements);
				}
				
				//process the simple processors after complex parsers (the complex parsers clean up the markdown)
				for (MarkdownElementParser parser : simpleParsers) {
					parser.processLine(elements);
				}
			}
				

			
			List<MarkdownElementParser> newActiveComplexParsers = new ArrayList<MarkdownElementParser>();
			List<MarkdownElementParser> newInactiveComplexParsers = new ArrayList<MarkdownElementParser>();
			//add all from the still processing list (maintain order)
			for (MarkdownElementParser parser : activeComplexParsers) {
				if (parser.isInMarkdownElement())
					newActiveComplexParsers.add(parser);
				else
					newInactiveComplexParsers.add(parser);
			}
			
			//sort the rest
			for (MarkdownElementParser parser : inactiveComplexParsers) {
				if (parser.isInMarkdownElement()) //add to the front (reverse their order so that they can have the opportunity to be well formed)
					newActiveComplexParsers.add(0, parser);
				else
					newInactiveComplexParsers.add(parser);
			}
			
			activeComplexParsers = newActiveComplexParsers;
			inactiveComplexParsers = newInactiveComplexParsers;
			
			output.append(elements.getHtml());
			//also tack on a <br />, unless we are a block element (those parsers handle their own newlines
			boolean isInMiddleOfBlockElement = false;
			for (MarkdownElementParser parser : parsers) {
				if (parser.isInMarkdownElement() && parser.isBlockElement()) {
					isInMiddleOfBlockElement = true;
					break;
				}
			}
			if (!isInMiddleOfBlockElement)
				output.append(ServerMarkdownUtils.HTML_LINE_BREAK);
		}
		
		for (MarkdownElementParser parser : parsers) {
			parser.completeParse(output);
		}
		return output.toString();
	}
	
	/**
	 * After markdown is converted into html, postprocess that html
	 * @param markdown
	 * @return
	 */
	public Widget postProcessHtml(String html) {
		Document doc = Document.get();
		HTMLPanel htmlPanel = new HTMLPanel(html);
		htmlPanel.addStyleName("markdown");
		
		for (MarkdownElementParser parser : allElementParsers) {
			parser.completeParse(doc);
		}
		ServerMarkdownUtils.assignIdsToHeadings(htmlPanel.getElement());
		return htmlPanel;
	}
}
