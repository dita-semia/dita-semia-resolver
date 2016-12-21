package org.DitaSemia.Ot.XsltConref;

import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Ot.DitaSemiaOtResolver;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;

public class ResolveXsltConrefCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(ResolveXsltConrefCall.class.getName());
	
	public static final String MISSING_CLASS_XPATH = "//*[not(@class) and not(ancestor::*[contains(@class, ' topic/foreign ')]) and not(self::no-content)]";


	protected final DitaSemiaOtResolver otResolver;

	public ResolveXsltConrefCall(DitaSemiaOtResolver otResolver) {
		this.otResolver	= otResolver;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		
		try {
			final Sequence argument = arguments[0].head();
			if (!(argument instanceof NodeInfo)) {
				throw new XPathException("Supplied parameter '" + argument + "' is no compatible node.");
			}
			final NodeInfo 			node 		= (NodeInfo)argument;
			final SaxonNodeWrapper	nodeWrapper	= new SaxonNodeWrapper(node, otResolver.getXPathCache());
			final XsltConref 		xsltConref 	= XsltConref.fromNode(nodeWrapper, otResolver.getXsltConrefTransformerCache(), otResolver.getXPathCache());
			//logger.info("xsltConref: " + xsltConref);
			if (xsltConref == null) {
				throw new XPathException("Supplied node '" + node.getLocalPart() + "' is no xslt-conref element.");
			}
			
			// set original URL as base URI to resolve relative URIs correctly
			xsltConref.setBaseUrl(otResolver.getCurrentBaseUrl());
			
			final NodeInfo resolvedNode 	= xsltConref.resolve(null);
			final NodeInfo resolvedElement 	= resolvedNode.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT).next();
	
			//logger.info("resolved: " + SaxonNodeWrapper.serializeNode(resolvedElement));
			
			if (XsltConref.getReparse(resolvedElement)) {
				
				logger.info("Reparsing...");
				
				// serializer roundtrip to set the default attributes
				try {
					
					final String 	serialized 		= SaxonNodeWrapper.serializeNode(resolvedNode);
					final Source 	source 			= new SAXSource(otResolver.getXsltConrefXmlReader(), new InputSource(new StringReader(serialized)));
					final XdmNode 	reparsedNode 	= otResolver.getDocumentBuilder().build(source);
					final NodeInfo 	reparsedElement = reparsedNode.getUnderlyingNode().iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT).next(); 
	
					checkResult(xsltConref, reparsedElement);
					
					// return the root element
					return reparsedElement;
					
				} catch (SaxonApiException e) {
					throw new XPathException("Failed to reparse resolved xslt-conref: " + e.getMessage());
				}
				
				
			} else {
				
				checkResult(xsltConref, resolvedElement);
				
				// return the root element
				return resolvedElement;
				
			}
		} catch (XPathException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e, e);
			return null;
		}
	}

	private void checkResult(final XsltConref xsltConref, final NodeInfo resolvedElement) {
		
		// check for missing class attribute
		try {
			final XPathExecutable 	xPath	= otResolver.getXPathCache().getXPathExecutable(MISSING_CLASS_XPATH);
			final XPathSelector		sel		= xPath.load();
			sel.setContextItem(new XdmNode(resolvedElement));
			final XdmItem item = sel.evaluateSingle();
			if (item != null) {
				String name;
				if (item instanceof XdmNode) {
					name = "node '" + ((XdmNode)item).getNodeName() + "'";
				} else {
					name = "item '" + item.getStringValue() + "'";
				}
				otResolver.getOtLogger().error("[DOT-DITA-SEMIA][ERROR] Missing class attribute in resolved xslt-conref '" + xsltConref.getScriptName() + "'. E.g. on " + name + ".");
			}
			
		} catch (SaxonApiException | XPathException e) {
			logger.error(e.getMessage(), e);
		}
		
		
	}
	
}
