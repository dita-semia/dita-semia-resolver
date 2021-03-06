package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefListInterface;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

public class AncestorPathCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(AncestorPathCall.class.getName());


	protected final KeyDefListInterface keyDefList;

	public AncestorPathCall(KeyDefListInterface keyDefList) {
		this.keyDefList	= keyDefList;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		
		try {
			final Item contextItem = context.getContextItem();
			if (!(contextItem instanceof NodeInfo)) {
				throw new XPathException("Context item '" + contextItem + "' is no compatible node.");
			}
			
			final NodeInfo			contextNode	= (NodeInfo)contextItem;
			final KeyDefInterface 	keyDef 		= Common.GetAncestorKeyDef(contextNode, arguments[0], keyDefList);
			
			return GetPathCall.getPath(keyDef);
			
		} catch (XPathException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException("Error: ", e.getMessage());
		}
	}

}
