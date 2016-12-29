package org.DitaSemia.Oxygen;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.DocumentCache;
import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.DocumentCaching.TopicRef;
import org.apache.log4j.Logger;

import ro.sync.ecss.css.StaticContent;
import ro.sync.ecss.css.StringContent;
import ro.sync.ecss.css.Styles;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class TopicNumStylesFilter extends DitaSemiaStylesFilter {


	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TopicNumStylesFilter.class.getName());
	
	private final static int	PSEUDO_LEVEL_TOPIC_NUM	= 20;
	
	private final static String	APPENDIX_NUM_DELIMITER	= ":"; 
	
	public static boolean filter(Styles styles, AuthorNode authorNode) {
		boolean handled = false;
		//logger.info("filter for node: " + authorNode.getName() + ", parent-node: " + (authorNode.getParent() == null ? "-" : authorNode.getParent().getName()) + ", pseudo-level: " + styles.getPseudoLevel());
		if (authorNode.getType() == AuthorNode.NODE_TYPE_PSEUDO_ELEMENT) {
			
			final boolean isBefore 	= (authorNode.getName().equals(BEFORE));
			
			if ((isBefore) && (styles.getPseudoLevel() == PSEUDO_LEVEL_TOPIC_NUM)) {
				handled = filterTopicNum(styles, authorNode.getParent());
			}
		}
		return handled;
	}

	private static boolean filterTopicNum(Styles styles, AuthorNode authorNode) {
		boolean handled = false;
		final NodeWrapper node = new AuthorNodeWrapper(authorNode, null);
		/*logger.info("filterTopicNum: " + authorNode.getDisplayName());
		logger.info("  class: " + node.getAttribute(DitaUtil.ATTR_CLASS, null));
		logger.info("  parent-class: " + node.getParent().getAttribute(DitaUtil.ATTR_CLASS, null));*/
		if (isTopicTitle(node)) {
			handled = true;
			
			final DocumentCache cache 	= getDocumentCache(authorNode);
			//logger.info("  cache: " + cache);
			
			if (cache != null) {
				final String topicNum = getTopicNum(node, cache);
				//logger.info("  topicNum: " + topicNum);
				if (topicNum != null) {
					StaticContent[] content = styles.getMixedContent();
					if ((content != null) && (content.length > 0)) {
						content[0] = new StringContent(topicNum.toString()); //change the first content element to the global topic number
					}
				} else {
					// no numbering -> remove complete prefix including separator between number and title
					styles.setProperty(Styles.KEY_MIXED_CONTENT, null);
				}
			} else {
				// don't modify anything
			}
		}
		return handled;
	}

	private static boolean isTopicTitle(NodeWrapper node) {
		final String classAttr	= node.getAttribute(DitaUtil.ATTR_CLASS, null);
		if ((classAttr != null) && (classAttr.contains(DitaUtil.CLASS_TITLE))) {
			final NodeWrapper parent = node.getParent();
			final String parentClassAttr = parent.getAttribute(DitaUtil.ATTR_CLASS, null);
			return ((parentClassAttr != null) && (parentClassAttr.contains(DitaUtil.CLASS_TOPIC)));
		} else {
			return false;
		}
	}
	
	private static boolean isLocalRootTopic(NodeWrapper node) {
		//logger.info("node " + node.getName() + ", " + node);
		//logger.info("getRootElement(): " + node.getRootElement().getName() + ", " + node.getRootElement());
		return node.getRootElement().isSameNode(node);
	}
	
	private static String getTopicNum(NodeWrapper node, DocumentCache cache) {
		final TopicRef 		topicRef = cache.getTopicRef(FileUtil.decodeUrl(node.getBaseUrl()));
		final StringBuffer 	topicNum = cache.getTopicNum(topicRef);
		if (topicNum == null) {
			return null;
		} else if ((topicRef.getType() == TopicRef.TYPE_APPENDIX) && (isLocalRootTopic(node.getParent()))) {
			return getAppendixPrefix() + " " + topicNum.toString() + APPENDIX_NUM_DELIMITER;
		} else {
			return topicNum.toString();
		}
	}
	
	static String appendixPrefix = null;
	
	public static String getAppendixPrefix() {
		if (appendixPrefix == null) {
			final String language = PluginWorkspaceProvider.getPluginWorkspace().getUserInterfaceLanguage();
			if (language.equals("de_DE")) {
				appendixPrefix = "Anhang";
			} else {
				appendixPrefix = "Appendix";
			}
		}
		return appendixPrefix;
	}
}
