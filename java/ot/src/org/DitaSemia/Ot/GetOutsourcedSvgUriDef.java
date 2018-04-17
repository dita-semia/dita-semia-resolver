package org.DitaSemia.Ot;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Ot.DitaSemiaOtResolver;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class GetOutsourcedSvgUriDef extends ExtensionFunctionDefinition {

	public static final String LOCAL_NAME		= "getOutsourcedSvgUri";

	protected final DitaSemiaOtResolver otResolver;
	
	public GetOutsourcedSvgUriDef(DitaSemiaOtResolver otResolver) {
		this.otResolver	= otResolver;
	}
	
	@Override
	public boolean dependsOnFocus() {
		return true;
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
		return SequenceType.SINGLE_ANY_URI;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new GetOutsourcedSvgUriDefCall(otResolver);
	}

}
