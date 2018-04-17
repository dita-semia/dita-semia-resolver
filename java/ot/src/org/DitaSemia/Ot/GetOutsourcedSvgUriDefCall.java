package org.DitaSemia.Ot;

import java.net.URI;

import org.DitaSemia.Ot.DitaSemiaOtResolver;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;

public class GetOutsourcedSvgUriDefCall extends ExtensionFunctionCall {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GetOutsourcedSvgUriDefCall.class.getName());

	protected final DitaSemiaOtResolver otResolver;

	public GetOutsourcedSvgUriDefCall(DitaSemiaOtResolver otResolver) {
		this.otResolver	= otResolver;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {

		final AnyURIValue uri = (AnyURIValue)arguments[0].head();
		
		final URI outUri = otResolver.getOutsourcedSvgUri(uri.asString());
		
		return new AnyURIValue(outUri.toString());
	}

}
