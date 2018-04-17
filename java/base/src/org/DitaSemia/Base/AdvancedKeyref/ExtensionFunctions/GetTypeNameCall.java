package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.apache.log4j.Logger;

import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;

public class GetTypeNameCall extends KeyDefExtensionFunctionCall {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GetTypeNameCall.class.getName());


	@Override
	public Sequence call(KeyDefInterface keyDef) throws XPathException {
		
		final String 			typeName 	= keyDef.getDxdTypeName();
		if (typeName != null) {
			return new StringValue(typeName);
		} else {
			return EmptySequence.getInstance();
		}
	}

}
