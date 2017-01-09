package org.DitaSemia.Oxygen;

import java.net.MalformedURLException;
import java.net.URL;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.ElementOverNodeInfo;
import net.sf.saxon.om.NodeInfo;

import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.XPathCache;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.apache.log4j.Logger;

public class SchematronUtil {

	private static final Logger logger = Logger.getLogger(SchematronUtil.class.getName());
	
	private static XPathCache 		xPathCache 	= null;


	public static NodeInfo evaluateXPathToNode(ElementOverNodeInfo currentElement, String xPath) {
		if(xPath != null) {
			try {
				final SaxonNodeWrapper result = (SaxonNodeWrapper)createSaxonNodeWrapper(currentElement).evaluateXPathToNode(xPath);
				if (result != null) {
					return result.getNodeInfo(); 
				} else {
					return null;
				}
			} catch (Exception e) {
				logger.error(e, e);
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static String evaluateXPathToStringList(ElementOverNodeInfo currentElement, String xPath) {
		if(xPath != null) {
			try {
				return String.join(KeyDef.PATH_DELIMITER, createSaxonNodeWrapper(currentElement).evaluateXPathToStringList(xPath));
			} catch (Exception e) {
				logger.error(e, e);
				return null;
			}
		} else {
			return null;
		}
	}


	public static String evaluateXPathToString(ElementOverNodeInfo currentElement, String xPath) {
		if(xPath != null) {
			try {
				return createSaxonNodeWrapper(currentElement).evaluateXPathToString(xPath);
			} catch (Exception e) {
				logger.error(e, e);
				return null;
			}
		} else {
			return null;
		}
	}
	
	protected static SaxonNodeWrapper createSaxonNodeWrapper(ElementOverNodeInfo currentElement) {
		NodeInfo node = currentElement.getUnderlyingNodeInfo();
		return new SaxonNodeWrapper(node, getXPathCache(currentElement));
	}
	
	private static XPathCache getXPathCache(ElementOverNodeInfo currentElement) {
		final Configuration elementConfig	= currentElement.getUnderlyingNodeInfo().getConfiguration();
		if ((xPathCache == null) || (!xPathCache.isCompatible(elementConfig))) {
			try {
				final BookCache 	bookCache 	= BookCacheHandler.getInstance().getBookCache(new URL(currentElement.getBaseURI()));
				final Configuration xPathConfig = bookCache.createConfiguration();
				xPathConfig.setNamePool(elementConfig.getNamePool());
				xPathConfig.setDocumentNumberAllocator(elementConfig.getDocumentNumberAllocator());
				xPathCache = BookCache.createXPathCache(xPathConfig);
			} catch (MalformedURLException e) {
				logger.error(e, e);
				xPathCache = null;
			}
		}
		return xPathCache;
	}
}
