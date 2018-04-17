package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.apache.log4j.Logger;

import net.sf.saxon.om.Sequence;
import net.sf.saxon.value.StringValue;

public class GetKeyCall extends KeyDefExtensionFunctionCall {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GetKeyCall.class.getName());


	@Override
	public Sequence call(KeyDefInterface keyDef) {
		return new StringValue(keyDef.getKey());
	}

}
