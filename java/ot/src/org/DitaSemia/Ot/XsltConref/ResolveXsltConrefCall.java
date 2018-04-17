package org.DitaSemia.Ot.XsltConref;

import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Ot.DitaSemiaOtResolver;
import org.apache.log4j.Logger;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.SaxonApiException;
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
			final XsltConref 		xsltConref 	= XsltConref.fromNode(nodeWrapper, otResolver.getXsltConrefCache(), false);
			//logger.info("xsltConref: " + xsltConref);
			if (xsltConref == null) {
				throw new XPathException("Supplied node '" + node.getLocalPart() + "' is no xslt-conref element.");
			}

			final NodeInfo resolvedNode = xsltConref.resolveToNode(null);
	
			//logger.info("resolved: " + SaxonNodeWrapper.serializeNode(resolvedElement));
			checkResult(xsltConref, resolvedNode);
			
			return resolvedNode;
			
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
			final XdmNode		context	= new XdmNode(resolvedElement);
			final XPathSelector sel		= otResolver.getXPathCache().getXPathSelector(MISSING_CLASS_XPATH, context);
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
