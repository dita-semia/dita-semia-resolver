package org.DitaSemia.Base.ExtensionFunctions;

import org.DitaSemia.Base.XPathCache;
import org.apache.log4j.Logger;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;

public class EvaluateXPathCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(EvaluateXPathCall.class.getName());

	protected static XPathCache xPathCache = null;
	
	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		//logger.info("call");

		final String 	xPath 		= arguments[0].head().getStringValue();
		final Sequence 	contextNode	= arguments[1].head();
		
		try {
			if (!(contextNode instanceof NodeInfo)) {
				throw new XPathException("Supplied 2nd parameter '" + contextNode + "' is no compatible node.");
			}
			
			final Configuration config = ((NodeInfo)contextNode).getConfiguration();
			if ((xPathCache == null) || (!xPathCache.isCompatible(config))) {
				logger.info("new Configuration");
				xPathCache = new XPathCache(config);
			}
			
			final XPathSelector selector = xPathCache.getXPathSelector(xPath, new XdmNode((NodeInfo)contextNode));

			return selector.evaluate().getUnderlyingValue();
			
		} catch (XPathException e) {
			throw new XPathException("Error evaluating XPath '" + xPath + "': " + e.getMessage());
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException("Error evaluating XPath '" + xPath + "': " + e.getMessage());
		}
	}

}
