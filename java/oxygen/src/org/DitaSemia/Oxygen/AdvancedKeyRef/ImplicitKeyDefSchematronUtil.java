package org.DitaSemia.Oxygen.AdvancedKeyRef;

import net.sf.saxon.dom.ElementOverNodeInfo;

import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
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
}
