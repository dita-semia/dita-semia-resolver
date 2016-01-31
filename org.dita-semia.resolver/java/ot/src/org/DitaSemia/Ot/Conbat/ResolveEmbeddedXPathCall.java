package org.DitaSemia.Ot.Conbat;

import org.DitaSemia.Base.EmbeddedXPathResolver;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Ot.DitaSemiaOtResolver;
import org.apache.log4j.Logger;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public class ResolveEmbeddedXPathCall extends ExtensionFunctionCall {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ResolveEmbeddedXPathCall.class.getName());

	protected final DitaSemiaOtResolver otResolver;

	public ResolveEmbeddedXPathCall(DitaSemiaOtResolver otResolver) {
		this.otResolver	= otResolver;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		
		final Item contextItem = context.getContextItem();
		if (!(contextItem instanceof NodeInfo)) {
			throw new XPathException("Context item '" + contextItem.getClass() + "'needs to be an instance of NodeInfo.");
		}
		
		final String 			xPath 			= arguments[0].head().getStringValue();
		final SaxonNodeWrapper 	contextNode 	= new SaxonNodeWrapper((NodeInfo)contextItem, otResolver.getXPathCache());
		final String			resolvedXPath	= EmbeddedXPathResolver.resolve(xPath, contextNode);
		
		return new StringValue(resolvedXPath);
	}
	
}
