package org.DitaSemia.Oxygen;


//import java.net.URL;

import java.net.URL;

import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Oxygen.BookCacheHandler;

import ro.sync.ecss.css.StaticContent;
import ro.sync.ecss.css.Styles;
import ro.sync.ecss.css.URIContent;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class DitaSemiaStylesFilter {

	protected final static String BEFORE	= "before";
	protected final static String AFTER		= "after";

	
	protected static BookCache getBookCache(AuthorNode authorNode) {
		final URL baseUrl = authorNode.getXMLBaseURL();
		if (baseUrl != null) {
			return BookCacheHandler.getInstance().getBookCache(baseUrl);
		} else {
			return null;
		}
	}
	
	protected static void ChangeContentUri(Styles styles, String fromImage, String toImage) {
		StaticContent[] content = (StaticContent[])styles.getProperty(Styles.KEY_MIXED_CONTENT);
		if (content != null) {
			for (int i = 0; i < content.length; ++i) {
				if ((content[i].getType() == 1/*StaticContent.URI_CONTENT*/) && (((URIContent)content[i]).getHref().contains(fromImage))) {
					URIContent uriContent = (URIContent)content[i];
					content[i] = new URIContent(uriContent.getBase(), uriContent.getHref().replace(fromImage, toImage));
				}
			}
		}
	}

}
