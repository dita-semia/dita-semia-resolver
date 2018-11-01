package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import org.DitaSemia.Base.AdvancedKeyref.KeyRefInterface;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class GetKeyDefByRefStringDef extends ExtensionFunctionDefinition {
	
	public static final String LOCAL_NAME	= "getKeyDefByRefString";

	protected final BookCacheProvider bookCacheProvider;
	
	public GetKeyDefByRefStringDef(BookCacheProvider 	bookCacheProvider) {
		this.bookCacheProvider	= bookCacheProvider;
	}

	@Override
		public SequenceType[] getArgumentTypes() {
			SequenceType[] sequenceType = {SequenceType.SINGLE_STRING, SequenceType.SINGLE_ANY_URI};
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
		return new GetKeyDefByRefStringCall(bookCacheProvider);
	}

}
