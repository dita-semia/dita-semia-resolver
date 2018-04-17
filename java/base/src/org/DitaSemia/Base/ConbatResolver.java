package org.DitaSemia.Base;

import org.apache.log4j.Logger;

import net.sf.saxon.trans.XPathException;

public class ConbatResolver {

	private static final Logger logger = Logger.getLogger(ConbatResolver.class.getName());

	public static final String NAMESPACE_PREFIX		= "cba";
	public static final String NAMESPACE_URI		= "http://www.dita-semia.org/conbat";
	
	public static final String ATTR_CONTENT			= "content";
	public static final String ATTR_DEFAULT_CONTENT	= "default-content";


	public static String getResolvedContent(NodeWrapper node) {
		String content = node.getTextContent();
		if ((content == null) || (content.isEmpty())) {
			content = node.getAttribute(ATTR_CONTENT, NAMESPACE_URI);
			if (content == null) {
				content = node.getAttribute(ATTR_DEFAULT_CONTENT, NAMESPACE_URI);
			}
			if (content != null) {
				try {
					content = EmbeddedXPathResolver.resolve(content, node);
				} catch (XPathException e) {
					logger.error (e, e);
				}
			}
		}
		return content;
	}
}
