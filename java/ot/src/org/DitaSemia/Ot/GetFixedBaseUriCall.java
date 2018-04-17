package org.DitaSemia.Ot;

import java.net.URL;

import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Ot.DitaSemiaOtResolver;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;

public class GetFixedBaseUriCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(GetFixedBaseUriCall.class.getName());

	protected final DitaSemiaOtResolver otResolver;

	public GetFixedBaseUriCall(DitaSemiaOtResolver otResolver) {
		this.otResolver	= otResolver;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {

		try {
			final Sequence 	node	= arguments[0].head();
			
			if (!(node instanceof NodeInfo)) {
				throw new XPathException("Supplied parameter '" + node + "' is no compatible node.");
			}
			
			final NodeWrapper 	nodeWrapper = new SaxonNodeWrapper((NodeInfo)node, null);
			final URL 			baseUrl		= FileUtil.getFixedBaseUrl(nodeWrapper, otResolver.getXsltConrefCache().getBaseDir());
			
			return new AnyURIValue(baseUrl.toString());
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException("Error in getFixedBaseUri(): " + e.getMessage());
		}
	}

}
