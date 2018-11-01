package org.DitaSemia.Base.ExtensionFunctions;

import java.net.URL;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.Common;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetPathCall;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

public class GetAncestorPathCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(GetAncestorPathCall.class.getName());


	protected final BookCacheProvider bookCacheProvider;

	public GetAncestorPathCall(BookCacheProvider bookCacheProvider) {
		this.bookCacheProvider	= bookCacheProvider;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		
		try {
			final Sequence 	nodeArgument	= arguments[0].head();
			if (!(nodeArgument instanceof NodeInfo)) {
				throw new XPathException("Supplied 1st parameter '" + nodeArgument + "' is no compatible node.");
			}
			
			final NodeInfo 			node 		= (NodeInfo)nodeArgument;
			final BookCache			bookCache	= bookCacheProvider.getBookCache(new URL(node.getBaseURI()));

			KeyDefInterface keyDef = null; 
			if (bookCache != null) {
				keyDef = Common.GetAncestorKeyDef(node, arguments[1], bookCache);
			}
							
			return GetPathCall.getPath(keyDef);
			
		} catch (XPathException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException("Error: ", e.getMessage());
		}
	}

}
