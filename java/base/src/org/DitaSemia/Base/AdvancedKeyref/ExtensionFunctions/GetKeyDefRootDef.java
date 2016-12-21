package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import org.DitaSemia.Base.DocumentCacheProvider;
import org.DitaSemia.Base.AdvancedKeyref.KeyRefInterface;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class GetKeyDefRootDef extends ExtensionFunctionDefinition {
	
	public static final String LOCAL_NAME	= "getKeyDefRoot"; 

	protected final DocumentCacheProvider documentCacheProvider;
	
	public GetKeyDefRootDef(DocumentCacheProvider 	documentCacheProvider) {
		this.documentCacheProvider	= documentCacheProvider;
	}

	@Override
		public SequenceType[] getArgumentTypes() {
			SequenceType[] sequenceType = {SequenceType.SINGLE_ELEMENT_NODE};
			return sequenceType;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(KeyRefInterface.NAMESPACE_PREFIX, KeyRefInterface.NAMESPACE_URI, LOCAL_NAME);
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.OPTIONAL_ELEMENT_NODE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new GetKeyDefRootCall(documentCacheProvider);
	}

}
