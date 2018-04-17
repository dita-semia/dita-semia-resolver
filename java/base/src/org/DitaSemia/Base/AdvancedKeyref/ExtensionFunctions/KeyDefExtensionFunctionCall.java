package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.ObjectValue;

public abstract class KeyDefExtensionFunctionCall extends ExtensionFunctionCall {

	public static KeyDefInterface getKeyDefFromItem(Item item) throws XPathException {
		if ((!(item instanceof ObjectValue<?>)) || (!(((ObjectValue<?>)item).getObject() instanceof KeyDefInterface))) {
			throw new XPathException("Supplied item (" + item + ") needs to be an instance of " + KeyDefInterface.class.getTypeName() + ".");
		}
		return (KeyDefInterface)(((ObjectValue<?>)item).getObject());
	}
	
	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		final Item keyDefItem = arguments[0].head();
		if (keyDefItem != null) {
			final KeyDefInterface 	keyDef 	= getKeyDefFromItem(keyDefItem);
			return call(keyDef, context, arguments);
		} else {
			return EmptySequence.getInstance();
		}
	}

	public Sequence call(KeyDefInterface keyDef, XPathContext context, Sequence[] arguments) throws XPathException {
		return call(keyDef);
	};
	
	public Sequence call(KeyDefInterface keyDef) throws XPathException {
		return EmptySequence.getInstance();
	};
	
}
