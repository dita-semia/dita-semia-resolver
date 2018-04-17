package org.DitaSemia.Base.ExtensionFunctions;


import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.XslTransformerCache;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class ExtractTextDef extends ExtensionFunctionDefinition {
	
	public static final String LOCAL_NAME	= "extractText"; 

	protected final XslTransformerCache transformerCache;
	
	public ExtractTextDef(XslTransformerCache transformerCache) {
		this.transformerCache = transformerCache;
	}
	
	@Override
	public SequenceType[] getArgumentTypes() {
		SequenceType[] sequenceType = {SequenceType.OPTIONAL_NODE};
		return sequenceType;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(DitaUtil.NAMESPACE_PREFIX, DitaUtil.NAMESPACE_URI, LOCAL_NAME);
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.OPTIONAL_STRING;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtractTextCall(transformerCache);
	}

}
