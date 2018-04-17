package org.DitaSemia.Oxygen.AdvancedKeyRef;

import net.sf.saxon.dom.ElementOverNodeInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Oxygen.BookCacheHandler;
import org.DitaSemia.Oxygen.SchematronUtil;
import org.apache.log4j.Logger;

public class ImplicitKeyDefSchematronUtil extends SchematronUtil {
	
	private static final Logger logger = Logger.getLogger(ImplicitKeyDefSchematronUtil.class.getName());
	
	public static KeyDef createKeyDef(ElementOverNodeInfo currentElement) {
		try {
			return KeyDef.fromNode(createSaxonNodeWrapper(currentElement));
		} catch (Exception e) {
			logger.error(e, e);
			return null;
		}
	}

	
	public static String getDefErrorMessage(KeyDefInterface keyDef) {
		Collection<KeyDefInterface> ambiguousKeyDefs = null;
		BookCache bookCache = BookCacheHandler.getInstance().getBookCache(keyDef.getDefUrl());
		if (bookCache != null) {
			ambiguousKeyDefs = bookCache.getAmbiguousKeyDefs(keyDef.getRefString());
		}
		if (ambiguousKeyDefs != null) {
			final String 		currLocation	= keyDef.getDefLocation();
			final List<String> 	locations 		= new ArrayList<>();
			for (KeyDefInterface ambigousKeyDef : ambiguousKeyDefs) {
				final String location = ambigousKeyDef.getDefLocation();
				if (!location.equals(currLocation)) {
					locations.add(location);
				}
			}
			return "Ambigous keydef. Other locations: " + String.join(", ", locations);
		} else {
			return null;
		}
	}
	
	
	public static String getXPathErrorMessage(ElementOverNodeInfo currentElement, String xPath) {
		if (xPath != null) {
			try {
				createSaxonNodeWrapper(currentElement).evaluateXPathToString(xPath);
				return null;
			} catch (Exception e) {
				logger.error(e, e);
				return e.getMessage();
			}
		} else {
			return null;
		}
	}
	
	
	public static String getXPathListErrorMessage(ElementOverNodeInfo currentElement, String xPath) {
		if (xPath != null) {
			try {
				createSaxonNodeWrapper(currentElement).evaluateXPathToStringList(xPath);
				return null;
			} catch (Exception e) {
				logger.error(e, e);
				return e.getMessage();
			}
		} else {
			return null;
		}
	}
	
	
	public static String getKeyDefID(ElementOverNodeInfo element) {
		return KeyDef.fromNode(createSaxonNodeWrapper(element)).getDefId();
	}

	
	public static KeyDefInterface getAncestorKeyDef(ElementOverNodeInfo element, String keyType) {
		final SaxonNodeWrapper node = new SaxonNodeWrapper(element.getUnderlyingNodeInfo(), null);
		final BookCache 		bookCache 	= BookCacheHandler.getInstance().getBookCache(node.getBaseUrl());
		return bookCache.getAncestorKeyDef(node, keyType);
	}
	
}
