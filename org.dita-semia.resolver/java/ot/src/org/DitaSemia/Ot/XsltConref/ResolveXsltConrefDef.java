package org.DitaSemia.Ot.XsltConref;

import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Ot.DitaSemiaOtResolver;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class ResolveXsltConrefDef extends ExtensionFunctionDefinition {
	
	public static final String LOCAL_NAME	= "resolve"; 

	protected final DitaSemiaOtResolver otResolver;
	
	public ResolveXsltConrefDef(DitaSemiaOtResolver otResolver) {
		this.otResolver	= otResolver;
	}

	@Override
		public SequenceType[] getArgumentTypes() {
			SequenceType[] sequenceType = {SequenceType.SINGLE_ELEMENT_NODE};
			return sequenceType;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(XsltConref.NAMESPACE_PREFIX, XsltConref.NAMESPACE_URI, LOCAL_NAME);
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_ELEMENT_NODE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new ResolveXsltConrefCall(otResolver);
	}

}
