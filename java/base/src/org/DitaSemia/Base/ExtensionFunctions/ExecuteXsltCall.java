package org.DitaSemia.Base.ExtensionFunctions;


import javax.xml.transform.Source;

import org.DitaSemia.Base.SaxonDocumentBuilder;
import org.DitaSemia.Base.XslTransformerCache;
import org.apache.log4j.Logger;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;

public class ExecuteXsltCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(ExecuteXsltCall.class.getName());

	private XslTransformerCache 	xslTransformerCache;
	private SaxonDocumentBuilder 	documentBuilder;

	public ExecuteXsltCall(SaxonDocumentBuilder documentBuilder) {
		this.documentBuilder = documentBuilder;
		xslTransformerCache = new XslTransformerCache(documentBuilder.getConfiguration());
		
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		//logger.info("call");

		final String inputUri	= arguments[0].head().getStringValue();
		final String xslUri		= arguments[1].head().getStringValue();
		
		
		try {
			
			final Configuration config = context.getConfiguration();
			
			final XsltExecutable 	xsltExecutable 	= xslTransformerCache.getExecutable(xslUri);
			final XsltTransformer 	xsltTransformer = xsltExecutable.load();
			final Source			inputSource		= config.getURIResolver().resolve(inputUri, "");
			final XdmNode 			inputNode 		= documentBuilder.build(inputSource, true, true);
			final XdmDestination 	destination 	= new XdmDestination();
			
			xsltTransformer.setInitialContextNode(inputNode);
			xsltTransformer.setDestination(destination);
			xsltTransformer.transform();
			
			final NodeInfo resolved = destination.getXdmNode().getUnderlyingNode();
			
			return resolved;
			
		} catch (XPathException e) {
			throw new XPathException("Error executing XSLT '" + xslUri + "' on '" + inputUri + "': " + e.getMessage());
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException("Error executing XSLT '" + xslUri + "' on '" + inputUri + "': " + e.getMessage());
		}
	}

}
