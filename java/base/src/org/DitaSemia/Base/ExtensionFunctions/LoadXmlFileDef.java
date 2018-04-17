package org.DitaSemia.Base.ExtensionFunctions;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.SaxonDocumentBuilder;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class LoadXmlFileDef extends ExtensionFunctionDefinition {
	
	public static final String LOCAL_NAME	= "loadXmlFile";
	
	protected final SaxonDocumentBuilder builder;
	
	public LoadXmlFileDef(SaxonDocumentBuilder builder) {
		this.builder = builder;
	}

	@Override
		public SequenceType[] getArgumentTypes() {
			SequenceType[] sequenceType = {SequenceType.SINGLE_STRING, SequenceType.SINGLE_BOOLEAN};
			return sequenceType;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(DitaUtil.NAMESPACE_PREFIX, DitaUtil.NAMESPACE_URI, LOCAL_NAME);
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.OPTIONAL_DOCUMENT_NODE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new LoadXmlFileCall(builder);
	}

}
