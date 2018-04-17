package org.DitaSemia.Base.ExtensionFunctions;

import org.DitaSemia.Base.DitaUtil;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class GetTextWidthDef extends ExtensionFunctionDefinition {
	
	public static final String LOCAL_NAME	= "getTextWidth"; 

	@Override
		public SequenceType[] getArgumentTypes() {
			SequenceType[] sequenceType = {SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING, SequenceType.SINGLE_BOOLEAN, SequenceType.SINGLE_DOUBLE};
			return sequenceType;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(DitaUtil.NAMESPACE_PREFIX, DitaUtil.NAMESPACE_URI, LOCAL_NAME);
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_DOUBLE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new GetTextWidthCall();
	}

}
