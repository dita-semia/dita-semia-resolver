package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import java.net.URL;

import org.DitaSemia.Base.DocumentCache;
import org.DitaSemia.Base.DocumentCacheProvider;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Base.AdvancedKeyref.KeyRefInterface;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;

public class GetKeyDefRootCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(GetKeyDefRootCall.class.getName());


	protected final DocumentCacheProvider 	documentCacheProvider;

	public GetKeyDefRootCall(DocumentCacheProvider documentCacheProvider) {
		this.documentCacheProvider	= documentCacheProvider;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		logger.info("call");
		try {
			final Sequence argument = arguments[0].head();
			if (!(argument instanceof NodeInfo)) {
				throw new XPathException("Supplied parameter '" + argument + "' is no compatible node.");
			}

			final NodeInfo 			node 			= (NodeInfo)argument;
			final DocumentCache		documentCache	= documentCacheProvider.getDocumentCache(new URL(node.getBaseURI()));
			final SaxonNodeWrapper	nodeWrapper		= new SaxonNodeWrapper(node, documentCache.getXPathCache());
			final KeyRefInterface 	keyRef			= KeyRef.fromNode(nodeWrapper);
			if (keyRef == null) {
				throw new XPathException("Supplied node '" + node.getLocalPart() + "' is no Advanced-KeyRef element.");
			}
			
			final KeyDefInterface	keyDef	= documentCache.getExactMatch(keyRef);
			if (keyDef != null) {
				final SaxonNodeWrapper root = (SaxonNodeWrapper)keyDef.getRoot();
				logger.info("done: " + root.getName());
				return root.getNodeInfo();
			} else {
				logger.info("done: empty");
				return EmptySequence.getInstance();
			}
		} catch (XPathException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException(e.getMessage());
		}
	}

}
