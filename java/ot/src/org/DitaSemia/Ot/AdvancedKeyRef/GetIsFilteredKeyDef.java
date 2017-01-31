package org.DitaSemia.Ot.AdvancedKeyRef;

import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Ot.DitaSemiaOtResolver;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class GetIsFilteredKeyDef extends ExtensionFunctionDefinition {
	
	public static final String LOCAL_NAME	= "getIsFilteredKey";

	protected final DitaSemiaOtResolver otResolver;
	
	public GetIsFilteredKeyDef(DitaSemiaOtResolver otResolver) {
		this.otResolver	= otResolver;
	}
	
	@Override
	public boolean dependsOnFocus() {
		return true;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		SequenceType[] sequenceType = {SequenceType.ANY_SEQUENCE};
		return sequenceType;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(KeyDef.NAMESPACE_PREFIX, KeyDef.NAMESPACE_URI, LOCAL_NAME);
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_BOOLEAN;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new GetIsFilteredKeyCall(otResolver);
	}

}
