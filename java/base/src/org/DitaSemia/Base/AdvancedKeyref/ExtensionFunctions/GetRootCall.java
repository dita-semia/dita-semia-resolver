package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.apache.log4j.Logger;

import net.sf.saxon.om.Sequence;
import net.sf.saxon.value.EmptySequence;

public class GetRootCall extends KeyDefExtensionFunctionCall {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GetRootCall.class.getName());


	protected final BookCacheProvider 	bookCacheProvider;

	public GetRootCall(BookCacheProvider bookCacheProvider) {
		this.bookCacheProvider	= bookCacheProvider;
	}


	@Override
	public Sequence call(KeyDefInterface keyDef) {

		final BookCache			bookCache		= bookCacheProvider.getBookCache(keyDef.getDefUrl());
		
		if (bookCache != null) {
			final NodeWrapper root = bookCache.getNodeByLocation(keyDef.getDefLocation());
			//logger.info("  location: " + keyDef.getDefLocation());
			if (root != null) {
				//logger.info("  root: " + root.getName());
				return ((SaxonNodeWrapper)root).getNodeInfo();
			}
		}
		return EmptySequence.getInstance();
	}

}
