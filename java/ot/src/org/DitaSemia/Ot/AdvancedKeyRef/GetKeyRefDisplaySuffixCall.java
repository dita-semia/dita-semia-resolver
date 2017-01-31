package org.DitaSemia.Ot.AdvancedKeyRef;

import java.util.LinkedList;
import java.util.List;

import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef.DisplaySuffix;
import org.DitaSemia.Ot.DitaSemiaOtResolver;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.StringValue;

public class GetKeyRefDisplaySuffixCall extends ExtensionFunctionCall {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GetKeyRefDisplaySuffixCall.class.getName());

	protected final DitaSemiaOtResolver otResolver;

	public GetKeyRefDisplaySuffixCall(DitaSemiaOtResolver otResolver) {
		this.otResolver	= otResolver;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {

		final SaxonNodeWrapper 	keyRefNode	= new SaxonNodeWrapper((NodeInfo)arguments[0].head(), otResolver.getXPathCache());
		final KeyRef 			keyRef		= KeyRef.fromNode(keyRefNode);
		final Item 				keyDefItem	= arguments[1].head();	

		if ((keyRef == null) || (keyDefItem == null)) {
			return EmptySequence.getInstance();
		} else {
			final KeyDefInterface 	keyDef 			= DitaSemiaOtResolver.getKeyDefFromItem(arguments[1].head());
			final DisplaySuffix		displaySuffix	= keyRef.getDisplaySuffix(keyDef, false);
			final List<Item> 		list 			= new LinkedList<>();
			
			list.add(new StringValue(displaySuffix.keySuffix));
			list.add(new StringValue(displaySuffix.nameSuffix));

			return new SequenceExtent(list);
		}
	}

}
