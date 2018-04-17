package org.DitaSemia.Base.ExtensionFunctions;

import org.DitaSemia.Base.EmbeddedXPathResolver;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.XPathCache;
import org.apache.log4j.Logger;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public class ResolveEmbeddedXPathCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(ResolveEmbeddedXPathCall.class.getName());

	protected static XPathCache xPathCache = null;
	
	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		//logger.info("call");

		final String 	value 		= arguments[0].head().getStringValue();
		final Sequence 	contextNode	= arguments[1].head();
		
		try {
			if (!(contextNode instanceof NodeInfo)) {
				throw new XPathException("Supplied 2nd parameter '" + contextNode + "' is no compatible node.");
			}
			
			final Configuration config = ((NodeInfo)contextNode).getConfiguration();
			if ((xPathCache == null) || (!xPathCache.isCompatible(config))) {
				//logger.info("new Configuration");
				xPathCache = new XPathCache(config);
			}
			
			final NodeWrapper 	nodeWrapper = new SaxonNodeWrapper((NodeInfo)contextNode, xPathCache);
			final String 		result 		= EmbeddedXPathResolver.resolve(value, nodeWrapper);
			//logger.info("ResolveEmbeddedXPathCall: " + value + " -> " + result);

			return new StringValue(result);
			
		} catch (XPathException e) {
			throw new XPathException("Error resolving embedded XPath for value '" + value + "': " + e.getMessage());
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException("Error resolving embedded XPath for value '" + value + "': " + e.getMessage());
		}
	}

}
