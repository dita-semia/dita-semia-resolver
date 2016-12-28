package org.DitaSemia.Oxygen.Conbat;

import org.DitaSemia.Base.EmbeddedXPathResolver;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.apache.log4j.Logger;

import net.sf.saxon.trans.XPathException;
import ro.sync.ecss.css.Styles;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.node.AuthorParentNode;
import ro.sync.exml.view.graphics.Color;

public class ConbatContentResolver {
	
	private static final Logger logger = Logger.getLogger(ConbatContentResolver.class.getName());


	public static String resolveContent(AuthorNode authorNode, AuthorAccess authorAccess) {
		//logger.info("getDisplayName: " + authorNode.getDisplayName());
		//logger.info("getName: " + authorNode.getName());
		//logger.info("getType: " + authorNode.getType());
		/*if (authorNode.getParent() != null) {
			logger.info("getParentName: " + authorNode.getParent().getName());
		}*/
		String resolvedContent = null;
		NodeWrapper node = new AuthorNodeWrapper(authorNode, authorAccess);
		if (node.isElement()) {
			try {
				final String content = node.getAttribute(ConbatStylesFilter.ATTR_CONTENT, ConbatStylesFilter.NAMESPACE_URI);
				//logger.info("content: " + content);
				if (content != null) {
					resolvedContent = EmbeddedXPathResolver.resolve(content, node);
				} else if (((AuthorParentNode)authorNode).getContentNodes().isEmpty()) {
					final String defaultContent = node.getAttribute(ConbatStylesFilter.ATTR_DEFAULT_CONTENT, ConbatStylesFilter.NAMESPACE_URI);
					//logger.info("defaultContent: " + defaultContent);
					if (defaultContent != null) {
						resolvedContent = EmbeddedXPathResolver.resolve(defaultContent, node);
					}
				}
			} catch(XPathException e) {
				final String errMsg = e.getMessage().replaceFirst(ConbatStylesFilter.ERR_MSG_REMOVE_REGEXP, "");
				resolvedContent = ConbatStylesFilter.ERR_PREFIX + errMsg + ConbatStylesFilter.ERR_SUFFIX;
			}
		}
		
		return resolvedContent;
	}
}
