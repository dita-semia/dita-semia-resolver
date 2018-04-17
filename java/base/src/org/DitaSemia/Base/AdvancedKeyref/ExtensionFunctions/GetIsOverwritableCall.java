package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.KeyDefExtensionFunctionCall;
import org.apache.log4j.Logger;

import net.sf.saxon.om.Sequence;
import net.sf.saxon.value.BooleanValue;

public class GetIsOverwritableCall extends KeyDefExtensionFunctionCall {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GetIsOverwritableCall.class.getName());

	@Override
	public Sequence call(KeyDefInterface keyDef) {
		return (keyDef.isOverwritable()) ? BooleanValue.TRUE : BooleanValue.FALSE;
	}

}
