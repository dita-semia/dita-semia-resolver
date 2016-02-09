package org.DitaSemia.Oxygen.Conbat;

import org.DitaSemia.Base.EmbeddedXPathResolver;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.apache.log4j.Logger;

import net.sf.saxon.trans.XPathException;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.node.AuthorParentNode;

public class ConbatContentResolver {
	
	private static final Logger logger = Logger.getLogger(ConbatContentResolver.class.getName());

	public static final String 	NAMESPACE_URI				= "http://www.dita-semia.org/conbat";
	public static final String 	NAMESPACE_PREFIX			= "cba";
	
	public static final String	ERR_TEXT					= "<ERR>";
	public static final String 	ATTR_CONTENT				= "content";
	public static final String 	ATTR_DEFAULT_CONTENT		= "default-content";

	public static String resolveContent(AuthorNode authorNode, AuthorAccess authorAccess) {
		logger.info("getDisplayName: " + authorNode.getDisplayName());
		logger.info("getName: " + authorNode.getName());
		logger.info("getType: " + authorNode.getType());
		logger.info("getParentName: " + authorNode.getParent().getName());
		String resolvedContent = null;
		NodeWrapper node = new AuthorNodeWrapper(authorNode, authorAccess);
		if (node.isElement()) {
			try {
				final String content = node.getAttribute(ATTR_CONTENT, NAMESPACE_URI);
				logger.info("content: " + content);
				if (content != null) {
					resolvedContent = EmbeddedXPathResolver.resolve(content, node);
				} else if (((AuthorParentNode)authorNode).getContentNodes().isEmpty()) {
					final String defaultContent = node.getAttribute(ATTR_DEFAULT_CONTENT, 	NAMESPACE_URI);
					logger.info("defaultContent: " + defaultContent);
					if (defaultContent != null) {
						resolvedContent = EmbeddedXPathResolver.resolve(defaultContent, node);
					}
				}
			} catch(XPathException e) {
				logger.error(e, e);
				resolvedContent = ERR_TEXT;
			}
		}
		
		return resolvedContent;
	}
}
