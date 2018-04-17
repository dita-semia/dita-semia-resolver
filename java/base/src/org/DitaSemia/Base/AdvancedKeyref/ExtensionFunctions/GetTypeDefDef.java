package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import org.DitaSemia.Base.XslTransformerCacheProvider;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class GetTypeDefDef extends ExtensionFunctionDefinition {
	
	public static final String LOCAL_NAME	= "getTypeDef"; 

	protected final BookCacheProvider 			bookCacheProvider;
	protected final XslTransformerCacheProvider	xslTransformerCacheProvider;
	
	public GetTypeDefDef(BookCacheProvider bookCacheProvider, XslTransformerCacheProvider xslTransformerCacheProvider) {
		this.bookCacheProvider				= bookCacheProvider;
		this.xslTransformerCacheProvider	= xslTransformerCacheProvider;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		SequenceType[] sequenceType = {SequenceType.SINGLE_ITEM};
		return sequenceType;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(KeyDef.DXD_NAMESPACE_PREFIX, KeyDef.DXD_NAMESPACE_URI, LOCAL_NAME);
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.OPTIONAL_ELEMENT_NODE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new GetTypeDefCall(bookCacheProvider, xslTransformerCacheProvider);
	}

}
