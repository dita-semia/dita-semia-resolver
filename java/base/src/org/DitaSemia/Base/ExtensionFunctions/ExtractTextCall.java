package org.DitaSemia.Base.ExtensionFunctions;

import java.io.StringWriter;

import org.DitaSemia.Base.XslTransformerCache;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.KeyDefExtensionFunctionCall;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;

public class ExtractTextCall extends KeyDefExtensionFunctionCall {
	
	public final static String SCRIPT_URI = "urn:dita-semia:xsl:extract-text-standalone.xsl";
	
	private static final Logger logger = Logger.getLogger(ExtractTextCall.class.getName());

	protected final XslTransformerCache transformerCache;
	
	public ExtractTextCall(XslTransformerCache transformerCache) {
		this.transformerCache = transformerCache;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {

		final XsltExecutable 	executable 	= transformerCache.getExecutable(SCRIPT_URI);
		final XsltTransformer	transformer	= executable.load();
		final StringWriter		result		= new StringWriter();
		final Processor			processor	= new Processor(context.getConfiguration());
		final Serializer		serializer	= processor.newSerializer(result);
		final Item 				contextNode = arguments[0].head();
		
		if (contextNode == null) {
			return EmptySequence.getInstance();
		} else {
			transformer.setInitialContextNode(new XdmNode((NodeInfo)contextNode));
			
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
