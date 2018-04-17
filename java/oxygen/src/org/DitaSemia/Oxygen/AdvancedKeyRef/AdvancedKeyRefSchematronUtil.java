package org.DitaSemia.Oxygen.AdvancedKeyRef;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.saxon.dom.ElementOverNodeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;

import org.DitaSemia.Base.FilterProperties;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.XPathCache;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Base.AdvancedKeyref.KeyRefInterface;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Oxygen.BookCacheHandler;
import org.DitaSemia.Oxygen.SchematronUtil;
import org.apache.log4j.Logger;

public class AdvancedKeyRefSchematronUtil extends SchematronUtil {

	private static final Logger logger = Logger.getLogger(AdvancedKeyRefSchematronUtil.class.getName());
	
	public static KeyRef createKeyRef(ElementOverNodeInfo element) {
		NodeInfo 			node 		= element.getUnderlyingNodeInfo();
		SaxonNodeWrapper 	nodeWrapper = new SaxonNodeWrapper(node, new XPathCache(node.getConfiguration()));
		return KeyRef.fromNode(nodeWrapper);
	}
	
	public static String getXPathListErrorMessage(ElementOverNodeInfo element, String xPath) {
		if (xPath != null) {
			try {
				createSaxonNodeWrapper(element).evaluateXPathToStringList(xPath);
				return null;
			} catch (Exception e) {
				logger.error(e, e);
				return e.getMessage();
			}
		} else {
			return null;
		}
	}
	
	public static boolean matchesNamespaceFilter(KeyRefInterface keyRef, KeyDefInterface keyDef) {
		return ((keyRef.getNamespaceFilter() != null) ? KeyRef.matchesNamespaceFilter(keyRef.getNamespaceFilter(), keyDef.getNamespaceList()) : true);
	}
	
	public static boolean textMatchesPath(String text, String path) {
		final String[] 	pathList 	= (path == null) ? null : path.split(KeyDef.PATH_DELIMITER);
		int 			pathPos		= (pathList == null) ? -1 : pathList.length - 1;
		
		if ((pathPos >= 0) && (pathList[pathPos].matches(text))) {
			// the key might contain a '.', so checkt for equalty of the whole text with the last path element first.
			return true;
		} else {
			final String[] 	textList 	= (text == null) ? null : text.split("[./]");
			int 			textPos		= (textList == null) ? -1 : textList.length - 1;
			
			while ((textPos >= 0) && (pathPos >= 0)) {
				if (!textList[textPos].equals(pathList[pathPos])) {
					return false;
				}
				--textPos;
				--pathPos;
			}
			if (textPos >= 0) {
				// no further path elements but more text element
				return false;
			} else {
				// all text path elements do match
				return true;
			}
		}
	}

	public static KeyDefInterface getMatchingKeyDef(String refString, URL url) {
		return BookCacheHandler.getInstance().getBookCache(url).getExactMatch(refString);
	}
	
	public static boolean matchesPathLen(String path, String pathLenStr, KeyRef keyRef) {
		if ((pathLenStr != null) && (!pathLenStr.isEmpty())) {
			final int pathLen = Integer.parseInt(pathLenStr);
			if (pathLen > 0) {
				final String[] split = path.split(KeyDef.PATH_DELIMITER);
				return (split.length == pathLen);
			} else if (pathLen == 0){
				return true;
			} else if (pathLen == -1) {
				//namespace elements + 1 key
				int fullLength = keyRef.getNamespaceList().size() + 1;
				//logger.info("fullLength: " + fullLength + "pathLen: " + keyRef.getPathLen());
				return (keyRef.getPathLen() == fullLength);
			} else {
				return false;
			}
		}
		return true;
	}

	public static String evaluateXPathOnMatchingKeyDefRootToString(String refString, String xPath, URL url) throws XPathException {
		final BookCache 		bookCache 	= BookCacheHandler.getInstance().getBookCache(url);
		final KeyDefInterface	keyDef		= bookCache.getExactMatch(refString);
		if (keyDef == null) {
			return null;
		} else {
			final SaxonNodeWrapper node = (SaxonNodeWrapper)bookCache.getNodeByLocation(keyDef.getDefLocation());
			if (node != null) {
				return node.evaluateXPathToString(xPath);
			} else {
				return null;
			}
		}
	}

	public static List<String> evaluateXPathOnMatchingKeyDefRootToStringList(String refString, String xPath, URL url) throws XPathException {
		final BookCache 		bookCache 	= BookCacheHandler.getInstance().getBookCache(url);
		final KeyDefInterface	keyDef		= bookCache.getExactMatch(refString);
		if (keyDef == null) {
			return null;
		} else {
			final SaxonNodeWrapper node = (SaxonNodeWrapper)bookCache.getNodeByLocation(keyDef.getDefLocation());
			if (node != null) {
				return node.evaluateXPathToStringList(xPath);
			} else {
				return null;
			}
		}
	}
	
	public static FilterProperties getFilterPropFromKeyRefs(String refString, String filterXPath, URL url) throws XPathException {
		//logger.info("getFilterPropFromKeyRefs(" + refString + ", " + filterXPath + ")");
		final BookCache 			bookCache 	= BookCacheHandler.getInstance().getBookCache(url);
		final Collection<NodeInfo>	refList		= bookCache.getKeyRefs(refString);
		//logger.info("  refList: " + refList);
		if ((refList != null) && (!refList.isEmpty())) {
			final FilterProperties 		filterProp	= new FilterProperties();
			if (filterXPath == null) {
				for (Iterator<NodeInfo> iterator = refList.iterator(); (iterator.hasNext()) && (!filterProp.isEmpty());) {
					final SaxonNodeWrapper refNode = new SaxonNodeWrapper(iterator.next(), null);
					filterProp.combine(FilterProperties.getFromNodeWithAncestors(refNode));
				}
			} else {
				try {
					final XPathSelector xPathSelector = bookCache.getXPathCache().getXPathSelector(filterXPath, new XdmNode(refList.iterator().next()));
					for (Iterator<NodeInfo> iterator = refList.iterator(); (iterator.hasNext()) && (!filterProp.isEmpty());) {
						final NodeInfo 	nodeInfo	= iterator.next();
						final XdmNode 	xdmNode 	= new XdmNode(nodeInfo);
						xPathSelector.setContextItem(xdmNode);
						final XdmItem result = xPathSelector.evaluateSingle();
						if ((result != null) && (result instanceof XdmAtomicValue) && (((XdmAtomicValue)result).getBooleanValue() == true)) {
							filterProp.combine(FilterProperties.getFromNodeWithAncestors(new SaxonNodeWrapper(nodeInfo, null)));
						}
					}
				} catch (SaxonApiException e) {
					throw new XPathException("Failed to evaluate XPath expression: '" + filterXPath + "'): " + e.getMessage());
				}
			}
			return (filterProp.isUndefined()) ? null : filterProp;
		} else {
			return null;
		}
	}
	
}
