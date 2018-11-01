package org.DitaSemia.Base.ExtensionFunctions;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class GetElementByHrefDef extends ExtensionFunctionDefinition {
	
	public static final String LOCAL_NAME	= "getElementByHref";

	protected final BookCacheProvider bookCacheProvider;
	
	public GetElementByHrefDef(BookCacheProvider 	bookCacheProvider) {
		this.bookCacheProvider	= bookCacheProvider;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		SequenceType[] sequenceType = {SequenceType.SINGLE_STRING, SequenceType.SINGLE_ANY_URI};
		return sequenceType;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(DitaUtil.NAMESPACE_PREFIX, DitaUtil.NAMESPACE_URI, LOCAL_NAME);
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.OPTIONAL_ELEMENT_NODE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new GetElementByHrefCall(bookCacheProvider);
	}

}
