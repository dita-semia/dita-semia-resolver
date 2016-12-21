package org.DitaSemia.Base.DocumentCaching;

import java.net.URL;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.AncestorPathCall;
import org.DitaSemia.Base.DocumentCache;
import org.DitaSemia.Base.DocumentCacheProvider;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public class GetAncestorPathCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(GetAncestorPathCall.class.getName());


	protected final DocumentCacheProvider documentCacheProvider;

	public GetAncestorPathCall(DocumentCacheProvider documentCacheProvider) {
		this.documentCacheProvider	= documentCacheProvider;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		
		try {
			final NodeInfo	node	= (NodeInfo)arguments[0].head();
			final String	keyType	= ((StringValue)arguments[1].head()).asString();
			
			final DocumentCache		documentCache	= documentCacheProvider.getDocumentCache(new URL(node.getBaseURI()));
			final SaxonNodeWrapper	nodeWrapper		= new SaxonNodeWrapper(node, documentCache.getXPathCache());
			final KeyDefInterface 	keyDef 			= documentCache.getAncestorKeyDef(nodeWrapper, keyType);
			
			return AncestorPathCall.getPath(keyDef);
			
		} catch (XPathException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException("Error: ", e.getMessage());
		}
	}

}
