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
	
	public static String getType(KeyRefInterface keyRef) {
		return keyRef.getType();
	}
	
	public static String getNamespace(KeyRefInterface keyRef) {
		return keyRef.getNamespace();
	}
	
	public static String getKey(KeyRefInterface keyRef) {
		return keyRef.getKey();
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
	
	public static String getKeyName(KeyDefInterface keyDef) {
		if (keyDef != null) {
			return keyDef.getName();
		} else {
			return "";
		}
	}
	
	public static boolean matchesRefText(KeyRefInterface keyRef) {
		String 		namespace		= keyRef.getNamespace();
		String 		key 			= keyRef.getKey();
		String[] 	textList 		= keyRef.getText().split(KeyDef.PATH_DELIMITER);
		String[] 	namespaceList	= null;
		if (namespace != null) {
			namespaceList = namespace.split(KeyDef.PATH_DELIMITER);
		}
		int 		textLength		= textList.length;
		int 		namespaceLength	= (namespaceList != null ? namespaceList.length : 0);
		if (key.equals(textList[textLength - 1]) && textLength == 1) {
			return true;
		} else if (key.equals(textList[textLength - 1]) && textLength > 1 && namespaceLength >= textLength - 1) {
			int count = 1;
			for (int i = textLength - 2; i >= 0; i--) {
				if (!namespaceList[namespaceList.length - count].equals(textList[i])) {
					return false;
				}
				count++;
			}
			return true;
		} else {
			return false;
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
