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
	
	public static boolean matchesRefText(KeyRefInterface keyRef) {
		String		namespace 		= keyRef.getNamespace();
		String[] 	namespaceList	= null;
		if (namespace != null) {
			namespaceList = namespace.split(KeyRef.PATH_DELIMITER);
		}
		String 		key 			= keyRef.getKey();
		String[] 	textList 		= (keyRef.getText().isEmpty() 	? null 	: keyRef.getText().split(KeyDef.PATH_DELIMITER));
		int 		textLength		= (textList 		== null 	? 0 	: textList.length);
		int 		namespaceLength	= (namespaceList 	== null 	? 0 	: namespaceList.length);
		
		if (key != null && !key.equals(textList[textLength - 1])) {
			return false;
		} else if (key == null) {
			return matchesNamespace(textList, namespaceList, textLength - 1, namespaceLength);
		} else if (key.equals(textList[textLength - 1])) {
			return matchesNamespace(textList, namespaceList, textLength - 2, namespaceLength);
		}
		return false;
	}
	
	private static boolean matchesNamespace(String[] text, String[] namespace, int lastIndex, int nsLength) {
		if (lastIndex < 0) {
			// no namespace in text content
			return true;
		} else {
			if (nsLength == 0) {
				// namespace in text content, but not in @akr:ref
				return false;
			} else {
				if (lastIndex + 1 > nsLength) {
					// namespace in text content has more elements than namespace in @akr:ref
					return false;
				}
				for (int i = lastIndex, j = namespace.length - 1; i >= 0 && j >= 0 ; i-- , j--) {
					if (!text[i].equals(namespace[j])) {
						return false;
					}
				}
			}
		}
		return true;
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
