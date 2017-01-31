package org.DitaSemia.Ot.AdvancedKeyRef;

import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Ot.DitaSemiaOtResolver;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;

public class GetIsKeyHiddenCall extends ExtensionFunctionCall {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GetIsKeyHiddenCall.class.getName());

	protected final DitaSemiaOtResolver otResolver;

	public GetIsKeyHiddenCall(DitaSemiaOtResolver otResolver) {
		this.otResolver	= otResolver;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		
		final KeyDefInterface 	keyDef = DitaSemiaOtResolver.getKeyDefFromItem(arguments[0].head());
		//logger.info(keyDef + ": " + keyDef.getKeyFilterAttrSet());
		return (keyDef.getKeyFilterAttrSet() == null) ? BooleanValue.TRUE : BooleanValue.FALSE;
	}

}
