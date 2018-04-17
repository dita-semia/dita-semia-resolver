package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import org.DitaSemia.Base.XslTransformerCacheProvider;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.apache.log4j.Logger;

import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

public class GetTypeDefCall extends KeyDefExtensionFunctionCall {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GetTypeDefCall.class.getName());

	protected final BookCacheProvider 			bookCacheProvider;
	protected final XslTransformerCacheProvider xslTransformerCacheProvider;

	public GetTypeDefCall(BookCacheProvider bookCacheProvider, XslTransformerCacheProvider xslTransformerCacheProvider) {
		this.bookCacheProvider				= bookCacheProvider;
		this.xslTransformerCacheProvider	= xslTransformerCacheProvider;
	}

	@Override
	public Sequence call(KeyDefInterface keyDef) throws XPathException {
		return keyDef.getDxdTypeDef(xslTransformerCacheProvider, bookCacheProvider);
	}

}
