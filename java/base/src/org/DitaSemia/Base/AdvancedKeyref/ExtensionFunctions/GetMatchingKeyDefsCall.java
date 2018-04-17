package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.StringValue;

public class GetMatchingKeyDefsCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(GetMatchingKeyDefsCall.class.getName());


	protected final BookCacheProvider 	bookCacheProvider;

	public GetMatchingKeyDefsCall(BookCacheProvider bookCacheProvider) {
		this.bookCacheProvider	= bookCacheProvider;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		//logger.info("call");
		try {
			final Set<String> 		typeFilter 		= new HashSet<>();
			final SequenceIterator 	typeIterator	= arguments[0].iterate();
			Item typeItem = typeIterator.next();
			while (typeItem != null) {
				//logger.info(" type: " + typeItem.getStringValue());
				typeFilter.add(typeItem.getStringValue());
				typeItem = typeIterator.next();
			}

			final List<String> 		namespaceFilter 	= new ArrayList<>();
			final SequenceIterator 	namespaceIterator	= arguments[1].iterate();
			Item namespaceItem = namespaceIterator.next();
			while (namespaceItem != null) {
				//logger.info(" namespace: " + namespaceItem.getStringValue());
				namespaceFilter.add(namespaceItem.getStringValue());
				namespaceItem = namespaceIterator.next();
			}

			final URL		refUrl		= new URL(((AnyURIValue)arguments[2].head()).getStringValue());
			final BookCache	bookCache	= bookCacheProvider.getBookCache(refUrl);

			//logger.info(" url: " + refUrl);
			final List<Item> list = new LinkedList<>();
			if (bookCache != null) {
				final Collection<KeyDefInterface> 	keyDefs = bookCache.getMatchingKeyDefs(typeFilter, namespaceFilter);
				for (KeyDefInterface keyDef : keyDefs) {
					//logger.info(" KeyDef: " + keyDef.getRefString());
					list.add(new StringValue(keyDef.getRefString()));
				}
			}
			return new SequenceExtent(list);
		} catch (XPathException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException(e.getMessage());
		}
	}

}
