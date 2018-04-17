package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import java.util.HashMap;

import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class GetKeyTypeDefDef extends ExtensionFunctionDefinition {
	
	public static final String LOCAL_NAME	= "getKeyTypeDef";

	protected final BookCacheProvider 			bookCacheProvider;
	protected final HashMap<String, Sequence> 	cache = new HashMap<>();
	
	public GetKeyTypeDefDef(BookCacheProvider bookCacheProvider) {
		this.bookCacheProvider	= bookCacheProvider;
	}
	
	@Override
	public boolean dependsOnFocus() {
		return true;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		SequenceType[] sequenceType = {SequenceType.SINGLE_ELEMENT_NODE};
		return sequenceType;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(KeyRef.NAMESPACE_PREFIX, KeyRef.NAMESPACE_URI, LOCAL_NAME);
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_ELEMENT_NODE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new GetKeyTypeDefCall(bookCacheProvider, cache);
	}

}
