package org.DitaSemia.Base.DocumentCaching;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class GetChildTopicsDef extends ExtensionFunctionDefinition {
	
	public static final String LOCAL_NAME	= "getChildTopics"; 

	protected final BookCacheProvider bookCacheProvider;
	
	public GetChildTopicsDef(BookCacheProvider bookCacheProvider) {
		this.bookCacheProvider	= bookCacheProvider;
	}

	@Override
		public SequenceType[] getArgumentTypes() {
			SequenceType[] sequenceType = {SequenceType.SINGLE_NODE};
			return sequenceType;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(BookCache.NAMESPACE_PREFIX, BookCache.NAMESPACE_URI, LOCAL_NAME);
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.NODE_SEQUENCE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new GetChildTopicsCall(bookCacheProvider);
	}

}
