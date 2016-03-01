package org.DitaSemia.Ot.Conbat;

import org.DitaSemia.Ot.DitaSemiaOtResolver;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class ResolveEmbeddedXPathDef extends ExtensionFunctionDefinition {
	
	public static final String NAMESPACE_PREFIX	= "cba";
	public static final String NAMESPACE_URI	= "http://www.dita-semia.org/conbat";
	public static final String LOCAL_NAME		= "resolveEmbeddedXPath";

	protected final DitaSemiaOtResolver otResolver;
	
	public ResolveEmbeddedXPathDef(DitaSemiaOtResolver otResolver) {
		this.otResolver	= otResolver;
	}
	
	@Override
	public boolean dependsOnFocus() {
		return true;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		SequenceType[] sequenceType = {SequenceType.SINGLE_STRING};
		return sequenceType;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(NAMESPACE_PREFIX, NAMESPACE_URI, LOCAL_NAME);
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_STRING;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new ResolveEmbeddedXPathCall(otResolver);
	}

}
