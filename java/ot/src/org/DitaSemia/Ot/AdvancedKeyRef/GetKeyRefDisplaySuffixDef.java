package org.DitaSemia.Ot.AdvancedKeyRef;

import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Ot.DitaSemiaOtResolver;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class GetKeyRefDisplaySuffixDef extends ExtensionFunctionDefinition {
	
	public static final String LOCAL_NAME	= "getKeyRefDisplaySuffix";

	protected final DitaSemiaOtResolver otResolver;
	
	public GetKeyRefDisplaySuffixDef(DitaSemiaOtResolver otResolver) {
		this.otResolver	= otResolver;
	}
	
	@Override
	public boolean dependsOnFocus() {
		return true;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		SequenceType[] sequenceType = {SequenceType.SINGLE_ELEMENT_NODE, SequenceType.ANY_SEQUENCE};
		return sequenceType;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(KeyRef.NAMESPACE_PREFIX, KeyRef.NAMESPACE_URI, LOCAL_NAME);
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.STRING_SEQUENCE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new GetKeyRefDisplaySuffixCall(otResolver);
	}

}
