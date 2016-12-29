package org.DitaSemia.Oxygen.Conbat;

import java.net.MalformedURLException;
import java.net.URL;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.DocumentCache;
import org.DitaSemia.Base.DocumentCaching.FileCache;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.DocumentCacheHandler;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class XRefLinkTextResolver {
	
	private static final Logger logger = Logger.getLogger(XRefLinkTextResolver.class.getName());

	public static String resolveReference(AuthorNode authorNode, AuthorAccess authorAccess) {
		String resolved = null;
		final AuthorNodeWrapper node 		= new AuthorNodeWrapper(authorNode, authorAccess);
		final String			attrClass	= node.getAttribute(DitaUtil.ATTR_CLASS, null);
		final String			attrHref	= node.getAttribute(DitaUtil.ATTR_HREF, null);
		if ((attrHref != null) && (!attrHref.isEmpty()) && (attrClass.contains(DitaUtil.CLASS_XREF)) && (attrHref.contains(DitaUtil.HREF_URL_ID_DELIMITER))) {
			final int 		hrefSplitPos 	= attrHref.indexOf(DitaUtil.HREF_URL_ID_DELIMITER);
			final String 	hrefUrl 		= attrHref.substring(0, hrefSplitPos);
			final String 	hrefId 			= attrHref.substring(hrefSplitPos + 1);
			//logger.info("hrefUrl: " + hrefUrl + ", hrefId: " + hrefId);
			
			URL refUrl;
			if (hrefUrl.isEmpty()) {
				refUrl = node.getBaseUrl();
			} else {
				try {
					refUrl = new URL(node.getBaseUrl(), hrefUrl);
				} catch (MalformedURLException e) {
					logger.error(e, e);
					refUrl = null;
				}
			}
			
			//logger.info("refUrl: " + refUrl);
			if (refUrl != null) {
				final DocumentCache documentCache 	= DocumentCacheHandler.getInstance().getDocumentCache(node.getBaseUrl());
				final FileCache		fileCache		= documentCache.getFile(refUrl);
				if (fileCache != null) {
					resolved = fileCache.getLinkTitle(hrefId);
				}
			}
		}
		return resolved;
	}
}
