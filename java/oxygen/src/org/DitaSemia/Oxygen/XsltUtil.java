package org.DitaSemia.Oxygen;

import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.apache.log4j.Logger;

public class XsltUtil {

	private static final Logger logger = Logger.getLogger(XsltUtil.class.getName());
	
	public static String getKeyDefLocation(String refString) {
		final BookCacheHandler	handler		= BookCacheHandler.getInstance();
		final BookCache 		bookCache 	= handler.getBookCache(handler.getCurrMapUrl());
		final KeyDefInterface	keyDef		= bookCache.getExactMatch(refString);
		logger.info("KeyDef-count: " + bookCache.getKeyDefs().size());
		logger.info("refString: " + refString + ", keyDef: " + keyDef);
		if (keyDef == null) {
			return null;
		} else {
			return keyDef.getDefLocation();
		}
	}
}
