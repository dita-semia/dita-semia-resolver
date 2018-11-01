package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import java.net.URL;

import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
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

public class GetKeyDefFromNodeCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(GetKeyDefFromNodeCall.class.getName());


	protected final BookCacheProvider 	bookCacheProvider;

	public GetKeyDefFromNodeCall(BookCacheProvider bookCacheProvider) {
		this.bookCacheProvider	= bookCacheProvider;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		//logger.info("call");
		try {
			final NodeInfo	node		= (NodeInfo)arguments[0].head();
			final String 	refUrlStrg 	= node.getBaseURI();
			final URL		refUrl		= refUrlStrg.isEmpty() ? null : new URL(refUrlStrg);

			//logger.info(" refString: " + refString);
			//logger.info(" refUrl: " + refUrl);
			
			
			final BookCache			bookCache	= bookCacheProvider.getBookCache(refUrl);
			final KeyDefInterface	keyDef		= (bookCache != null) ? KeyDef.fromNode(new SaxonNodeWrapper(node, bookCache.getXPathCache())) : null;
			if (keyDef != null) {
				return new ObjectValue<KeyDefInterface>(keyDef);
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
