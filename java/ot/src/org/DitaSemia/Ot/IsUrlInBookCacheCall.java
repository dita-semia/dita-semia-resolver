package org.DitaSemia.Ot;

import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.DitaSemia.Base.DocumentCaching.FileCache;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceExtent;

public class IsUrlInBookCacheCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(IsUrlInBookCacheCall.class.getName());


	protected final BookCache bookCache;

	public IsUrlInBookCacheCall(BookCache bookCache) {
		this.bookCache	= bookCache;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		
		try {

			final URL	url		= new URL(((AnyURIValue)arguments[0].head()).getStringValue());
			return (bookCache.isUrlIncluded(url)) ? BooleanValue.TRUE : BooleanValue.FALSE;
			
		} catch (XPathException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException("Error: ", e.getMessage());
		}
	}

}
