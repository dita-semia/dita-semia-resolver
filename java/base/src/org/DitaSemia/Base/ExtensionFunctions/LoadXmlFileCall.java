package org.DitaSemia.Base.ExtensionFunctions;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.DitaSemia.Base.SaxonDocumentBuilder;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.Initializer;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;

public class LoadXmlFileCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(LoadXmlFileCall.class.getName());

	protected final SaxonDocumentBuilder builder;
	
	public LoadXmlFileCall(SaxonDocumentBuilder builder) {
		this.builder = builder;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		
		if (!builder.isCompatible(context.getConfiguration())) {
			throw new XPathException("Configuration is not compatible with " + SaxonDocumentBuilder.class.getName() + ". Create a " + Initializer.class.getName() + " and call SaxonDocumentBuilder.makeConfigurationCompatible().");
		}
		
		final String 	uriString 				= arguments[0].head().getStringValue();
		final boolean 	expandAttributeDefaults = ((BooleanValue)arguments[1].head()).getBooleanValue();
		
		try {
			final Source 	source 		= context.getURIResolver().resolve(uriString, "");
			final XdmNode 	document 	= builder.build(source, expandAttributeDefaults, true);
			
			return document.getUnderlyingNode();
			
		} catch (TransformerException | SaxonApiException e) {
			logger.error(e);
			return EmptySequence.getInstance();
		}
	}

}
