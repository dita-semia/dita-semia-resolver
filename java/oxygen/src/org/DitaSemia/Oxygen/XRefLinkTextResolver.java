package org.DitaSemia.Oxygen;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.Href;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.FileCache;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class XRefLinkTextResolver {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(XRefLinkTextResolver.class.getName());

	public static String resolveReference(AuthorNode authorNode, AuthorAccess authorAccess) {
		String resolved = null;
		final AuthorNodeWrapper node 		= new AuthorNodeWrapper(authorNode, authorAccess);
		final String			attrClass	= node.getAttribute(DitaUtil.ATTR_CLASS, null);
		if ((attrClass != null) && (attrClass.contains(DitaUtil.CLASS_XREF))) {
			final Href href = new Href(node.getAttribute(DitaUtil.ATTR_HREF, null), node.getBaseUrl());
						
			//logger.info("refUrl: " + refUrl);
			if (href.getRefUrl() != null) {
				final BookCache	bookCache 	= BookCacheHandler.getInstance().getBookCache(node.getBaseUrl());
				final FileCache	fileCache	= (bookCache != null ? bookCache.getFile(href.getRefUrl()) : null);
				if (fileCache != null) {
					resolved = fileCache.getLinkText(href.getRefId(), node);
				}
			}
		}
		return resolved;
	}
}
