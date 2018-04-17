package org.DitaSemia.Oxygen;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.FileCache;
import org.DitaSemia.Oxygen.Conbat.ConbatStylesFilter;
import org.apache.log4j.Logger;

import ro.sync.ecss.css.StaticContent;
import ro.sync.ecss.css.StringContent;
import ro.sync.ecss.css.Styles;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class TopicNumStylesFilter extends DitaSemiaStylesFilter {


	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TopicNumStylesFilter.class.getName());
	
	private final static int	PSEUDO_LEVEL_TOPIC_NUM	= 20;

	public static boolean filter(Styles styles, AuthorNode authorNode) {
		boolean handled = false;
		//logger.info("filter for node: " + authorNode.getName() + ", parent-node: " + (authorNode.getParent() == null ? "-" : authorNode.getParent().getName()) + ", pseudo-level: " + styles.getPseudoLevel());
		if (authorNode.getType() == AuthorNode.NODE_TYPE_PSEUDO_ELEMENT) {
			
			final boolean isBefore 		= (authorNode.getName().equals(BEFORE));
			final int		pseudoLevel	= styles.getPseudoLevel();
			
			if (isBefore) {
				if (pseudoLevel == PSEUDO_LEVEL_TOPIC_NUM) {
					handled = filterTopicNum(styles, authorNode.getParent());
				} else if (pseudoLevel == ConbatStylesFilter.PSEUDO_LEVEL_TITLE) {
					handled = filterCbaTitleTopicNum(styles, authorNode.getParent());
				}
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
			final BookCache cache 	= getBookCache(authorNode);
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

	private static boolean filterCbaTitleTopicNum(Styles styles, AuthorNode authorNode) {
		final NodeWrapper node = new AuthorNodeWrapper(authorNode, null);
		//logger.info("filterTopicNum: " + authorNode.getDisplayName());
		//logger.info("  class: " + node.getAttribute(DitaUtil.ATTR_CLASS, null));
		//logger.info("  cba-title: " + node.getParent().getAttribute(ConbatStylesFilter.ATTR_TITLE, ConbatStylesFilter.NAMESPACE_URI));
		if (isCbaTopicTitle(node)) {
			final BookCache cache 	= getBookCache(authorNode);
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
					// no numbering -> set topic num and seperator to empty string
					StaticContent[] content = styles.getMixedContent();
					if ((content != null) && (content.length > 1)) {
						content[0] = new StringContent("");
						content[1] = new StringContent("");
					}
				}
			} else {
				// don't modify anything
			}
		}
		return false; // handle CBA as well
	}

	private static boolean isCbaTopicTitle(NodeWrapper node) {
		final String classAttr	= node.getAttribute(DitaUtil.ATTR_CLASS, null);
		if ((classAttr != null) && (classAttr.contains(DitaUtil.CLASS_TOPIC))) {
			final String titleAttr	= node.getAttribute(ConbatStylesFilter.ATTR_TITLE, ConbatStylesFilter.NAMESPACE_URI);
			return ((titleAttr != null) && (!titleAttr.isEmpty()));
		} else {
			return false;
		}
	}
	
	private static boolean isLocalRootTopic(NodeWrapper node) {
		//logger.info("node " + node.getName() + ", " + node);
		//logger.info("getRootElement(): " + node.getRootElement().getName() + ", " + node.getRootElement());
		return node.getRootElement().isSameNode(node);
	}
	
	private static String getTopicNum(NodeWrapper node, BookCache bookCache) {
		final FileCache	fileCache	= (bookCache != null ? bookCache.getFile(node.getBaseUrl()) : null);
		if (fileCache == null) {
			return null;
		} else if (isLocalRootTopic(node.getParent())) {
			return fileCache.getRootTopicNumPrefix();
		} else {
			return fileCache.getRootTopicNum();
		}
	}
}
