package org.DitaSemia.Base.ExtensionFunctions;

import java.net.URL;

import org.DitaSemia.Base.Href;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.DitaSemia.Base.DocumentCaching.FileCache;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;

public class GetElementByHrefCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(GetElementByHrefCall.class.getName());


	protected final BookCacheProvider 	bookCacheProvider;

	public GetElementByHrefCall(BookCacheProvider bookCacheProvider) {
		this.bookCacheProvider	= bookCacheProvider;
	}


	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		try {
			final String 	hrefString 	= ((StringValue)arguments[0].head()).getStringValue();
			final String 	baseUrlStrg = ((AnyURIValue)arguments[1].head()).getStringValue();
			final URL		baseUrl		= baseUrlStrg.isEmpty() ? null : new URL(baseUrlStrg);

			final Href href = new Href(hrefString, baseUrl);

			if (href.getRefUrl() != null) {
				final BookCache	bookCache	= bookCacheProvider.getBookCache(baseUrl);
				final FileCache	fileCache	= (bookCache != null ? bookCache.getFile(href.getRefUrl()) : null);
				if (fileCache != null) {
					final SaxonNodeWrapper node = fileCache.getElementByRefId(href.getRefId());
					if (node != null) {
						return node.getNodeInfo();
					}
				}
			}
			
			return EmptySequence.getInstance();
			
		} catch (XPathException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException(e.getMessage());
		}
	}

}
