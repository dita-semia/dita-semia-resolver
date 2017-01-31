package org.DitaSemia.Ot.AdvancedKeyRef;

import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Ot.DitaSemiaOtResolver;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.ObjectValue;

public class GetReferencedKeyDefCall extends ExtensionFunctionCall {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GetReferencedKeyDefCall.class.getName());

	protected final DitaSemiaOtResolver otResolver;

	public GetReferencedKeyDefCall(DitaSemiaOtResolver otResolver) {
		this.otResolver	= otResolver;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {

		final SaxonNodeWrapper 	keyRefNode	= new SaxonNodeWrapper((NodeInfo)arguments[0].head(), otResolver.getXPathCache());
		final KeyRef 			keyRef		= KeyRef.fromNode(keyRefNode);
		final KeyDefInterface 	keyDef		= otResolver.getBookCache().getExactMatch(keyRef);
		
		if (keyDef == null) {
			return EmptySequence.getInstance();
		} else {
			otResolver.notifyKeyDefReferenced(keyDef);
			return new ObjectValue<KeyDefInterface>(keyDef);
		}
	}

}
