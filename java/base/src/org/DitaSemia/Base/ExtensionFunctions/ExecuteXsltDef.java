package org.DitaSemia.Base.ExtensionFunctions;


import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.SaxonDocumentBuilder;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class ExecuteXsltDef extends ExtensionFunctionDefinition {
	
	public static final String LOCAL_NAME	= "executeXslt"; 

	private SaxonDocumentBuilder documentBuilder;

	public ExecuteXsltDef(SaxonDocumentBuilder documentBuilder) {
		this.documentBuilder = documentBuilder;
	}
	
	@Override
	public SequenceType[] getArgumentTypes() {
		SequenceType[] sequenceType = {SequenceType.SINGLE_ANY_URI, SequenceType.SINGLE_ANY_URI};
		return sequenceType;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(DitaUtil.NAMESPACE_PREFIX, DitaUtil.NAMESPACE_URI, LOCAL_NAME);
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_NODE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new ExecuteXsltCall(documentBuilder);
	}

}
