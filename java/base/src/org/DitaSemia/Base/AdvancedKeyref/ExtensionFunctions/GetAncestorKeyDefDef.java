package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import org.DitaSemia.Base.AdvancedKeyref.KeyRefInterface;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class GetAncestorKeyDefDef extends ExtensionFunctionDefinition {

	public static final String LOCAL_NAME	= "getAncestorKeyDef"; 

	protected final BookCacheProvider bookCacheProvider;
	
	public GetAncestorKeyDefDef(BookCacheProvider 	bookCacheProvider) {
		this.bookCacheProvider	= bookCacheProvider;
	}

	@Override
		public SequenceType[] getArgumentTypes() {
			SequenceType[] sequenceType = {SequenceType.SINGLE_NODE, SequenceType.STRING_SEQUENCE};
			return sequenceType;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(KeyRefInterface.NAMESPACE_PREFIX, KeyRefInterface.NAMESPACE_URI, LOCAL_NAME);
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.OPTIONAL_ITEM;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new GetAncestorKeyDefCall(bookCacheProvider);
	}

}