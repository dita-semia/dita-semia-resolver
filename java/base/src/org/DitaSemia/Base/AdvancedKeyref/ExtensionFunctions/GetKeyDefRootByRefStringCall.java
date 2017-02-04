package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import java.net.URL;

import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;

public class GetKeyDefRootByRefStringCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(GetKeyDefRootByRefStringCall.class.getName());


	protected final BookCacheProvider 	bookCacheProvider;

	public GetKeyDefRootByRefStringCall(BookCacheProvider bookCacheProvider) {
		this.bookCacheProvider	= bookCacheProvider;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		logger.info("call");
		try {
			final String 	refString 	= ((StringValue)arguments[0].head()).getStringValue();
			final URL		refUrl		= new URL(((AnyURIValue)arguments[1].head()).getStringValue());
			
			logger.info(" refString: " + refString);
			logger.info(" refUrl: " + refUrl);
			
			final BookCache			bookCache	= bookCacheProvider.getBookCache(refUrl);
			final KeyDefInterface	keyDef		= bookCache.getExactMatch(refString);
			if (keyDef != null) {
				final NodeWrapper root = bookCache.getNodeByLocation(keyDef.getDefLocation());
				if (root != null) {
					return ((SaxonNodeWrapper)root).getNodeInfo();
				} else {
					return EmptySequence.getInstance();
				}
			} else {
				//logger.info("done: empty");
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
