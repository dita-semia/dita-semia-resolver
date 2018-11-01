package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import java.net.URL;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.ObjectValue;

public class GetAncestorKeyDefCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(GetAncestorKeyDefCall.class.getName());


	protected final BookCacheProvider 	bookCacheProvider;

	public GetAncestorKeyDefCall(BookCacheProvider bookCacheProvider) {
		this.bookCacheProvider	= bookCacheProvider;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		try {
			final Sequence 	nodeArgument	= arguments[0].head();
			if (!(nodeArgument instanceof NodeInfo)) {
				throw new XPathException("Supplied 1st parameter '" + nodeArgument + "' is no compatible node.");
			}
			
			final NodeInfo 	node 		= (NodeInfo)nodeArgument;
			final BookCache	bookCache	= bookCacheProvider.getBookCache(new URL(node.getBaseURI()));

			if (bookCache != null) {
				final KeyDefInterface keyDef = Common.GetAncestorKeyDef(node, arguments[1], bookCache);
				//logger.info("GetAncestorKeyDef: " + keyDef);
				if (keyDef != null) {
					return new ObjectValue<KeyDefInterface>(keyDef);
				} else {
					//logger.info("done: empty");
					return EmptySequence.getInstance();
				}
			} else {
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
