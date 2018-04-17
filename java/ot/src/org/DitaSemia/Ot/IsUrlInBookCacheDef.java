package org.DitaSemia.Ot;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.DocumentCaching.BookCache;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class IsUrlInBookCacheDef extends ExtensionFunctionDefinition {
	
	public static final String LOCAL_NAME	= "isUrlInBookCache"; 

	protected final BookCache bookCache;
	
	public IsUrlInBookCacheDef(BookCache bookCache) {
		this.bookCache	= bookCache;
	}

	@Override
		public SequenceType[] getArgumentTypes() {
			SequenceType[] sequenceType = {SequenceType.SINGLE_ANY_URI};
			return sequenceType;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(DitaUtil.NAMESPACE_PREFIX, DitaUtil.NAMESPACE_URI, LOCAL_NAME);
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_BOOLEAN;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new IsUrlInBookCacheCall(bookCache);
	}

}
