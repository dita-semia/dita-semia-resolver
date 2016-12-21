package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefListInterface;
import org.apache.log4j.Logger;

public class Common {
	
	private static final Logger logger = Logger.getLogger(Common.class.getName());

	public static KeyDefInterface GetAncestorKeyDef(XPathContext context, Sequence[] arguments, KeyDefListInterface keyDefList) throws XPathException {
		try {
			final Item contextItem = context.getContextItem();
			if (!(contextItem instanceof NodeInfo)) {
				throw new XPathException("Context item '" + contextItem + "' is no compatible node.");
			}
			
			final String			keyType			= ((StringValue)arguments[0].head()).asString();
			final NodeInfo			contextNode		= (NodeInfo)contextItem;
			final SaxonNodeWrapper	contextWrapper	= new SaxonNodeWrapper(contextNode, keyDefList.getXPathCache());

			//logger.info("Context Node: " + contextNode.getDisplayName());
			//logger.info("Context URL: " + contextNode.getBaseURI());

			return keyDefList.getAncestorKeyDef(contextWrapper, keyType);
		} catch (XPathException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e, e);
			return null;
		}
	}
	
}
