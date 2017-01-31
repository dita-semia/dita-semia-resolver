package org.DitaSemia.Oxygen.AdvancedKeyRef;

import java.net.URL;

import net.sf.saxon.dom.ElementOverNodeInfo;
import net.sf.saxon.om.NodeInfo;

import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.XPathCache;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Base.AdvancedKeyref.KeyRefInterface;
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
		final String[] 	textList = (text == null) ? null : text.split("[./]");
		final String[] 	pathList = (path == null) ? null : path.split(KeyDef.PATH_DELIMITER);
		
		int textPos	= (textList == null) ? -1 : textList.length - 1;
		int pathPos	= (pathList == null) ? -1 : pathList.length - 1;
		
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

	public static KeyDefInterface getMatchingKeyDef(String refString, URL url) {
		return BookCacheHandler.getInstance().getBookCache(url).getExactMatch(refString);
	}
	
	public static boolean matchesPathLen(String path, String pathLenStr) {
		if ((pathLenStr != null) && (!pathLenStr.isEmpty())) {
			final int pathLen = Integer.parseInt(pathLenStr);
			if (pathLen > 0) {
				final String[] split = path.split(KeyDef.PATH_DELIMITER);
				return (split.length == pathLen);
			} else {
				return false;
			}
		}
		return true;
	}
}
