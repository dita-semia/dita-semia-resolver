package org.DitaSemia.Base.ExtensionFunctions;

import java.io.StringWriter;

import org.DitaSemia.Base.XslTransformerCacheProvider;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;

public class XsltExtensionFunctionCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(XsltExtensionFunctionCall.class.getName());

	protected final XslTransformerCacheProvider xslTransformerCacheProvider;
	protected final String						scriptUri;
	protected final String						initialMode;
	
	public XsltExtensionFunctionCall(XslTransformerCacheProvider xslTransformerCacheProvider, String scriptUri, String initialMode) {
		this.xslTransformerCacheProvider 	= xslTransformerCacheProvider;
		this.scriptUri						= scriptUri;
		this.initialMode					= initialMode;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {

		final XsltExecutable 	executable 	= xslTransformerCacheProvider.getXslTransformerCache().getExecutable(scriptUri);
		final XsltTransformer	transformer	= executable.load();
		final StringWriter		result		= new StringWriter();
		final Processor			processor	= new Processor(context.getConfiguration());
		final Serializer		serializer	= processor.newSerializer(result);
		final Item 				contextNode = arguments[0].head();
		
		if (contextNode == null) {
			return EmptySequence.getInstance();
		} else {
			transformer.setInitialContextNode(new XdmNode((NodeInfo)contextNode));
			transformer.setInitialMode(new QName(initialMode));
			
			serializer.setOutputProperty(Serializer.Property.METHOD, "text");
			transformer.setDestination(serializer);
			try {
				transformer.transform();
				return new StringValue(result.toString());
			} catch (SaxonApiException e) {
				logger.error(e, e);
				throw new XPathException(e.getMessage());
			}
		}
	}

}
