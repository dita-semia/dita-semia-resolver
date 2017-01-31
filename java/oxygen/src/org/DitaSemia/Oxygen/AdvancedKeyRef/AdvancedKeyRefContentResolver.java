package org.DitaSemia.Oxygen.AdvancedKeyRef;

import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.BookCacheHandler;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class AdvancedKeyRefContentResolver {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AdvancedKeyRefContentResolver.class.getName());
	
	public static String resolveContent(AuthorNode node, AuthorAccess authorAccess) {

		KeyRef keyRef = KeyRef.fromNode(new AuthorNodeWrapper(node, authorAccess));
		if (keyRef != null) {
			final BookCache cache = BookCacheHandler.getInstance().getBookCache(node.getXMLBaseURL());
			return keyRef.getDisplaySuffix(cache, true).toString();
		} else {
			return null;
		}
	}
}
