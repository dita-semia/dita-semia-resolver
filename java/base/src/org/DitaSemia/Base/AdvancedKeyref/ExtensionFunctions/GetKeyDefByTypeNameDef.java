package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class GetKeyDefByTypeNameDef extends ExtensionFunctionDefinition {
	
	public static final String LOCAL_NAME	= "getKeyDefByTypeName"; 

	protected final BookCacheProvider bookCacheProvider;
	
	public GetKeyDefByTypeNameDef(BookCacheProvider 	bookCacheProvider) {
		this.bookCacheProvider	= bookCacheProvider;
	}

	@Override
		public SequenceType[] getArgumentTypes() {
			SequenceType[] sequenceType = {SequenceType.SINGLE_STRING, SequenceType.SINGLE_ANY_URI};
			return sequenceType;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(KeyDefInterface.DXD_NAMESPACE_PREFIX, KeyDefInterface.DXD_NAMESPACE_URI, LOCAL_NAME);
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.OPTIONAL_ITEM;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new GetKeyDefByTypeNameCall(bookCacheProvider);
	}

}
