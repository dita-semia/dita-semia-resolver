package org.DitaSemia.Oxygen.AdvancedKeyRef.CustomFunctions;

import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.Source;
import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.SaxonDocumentBuilder;
import org.DitaSemia.Base.XPathCache;
import org.DitaSemia.Base.XslTransformerCache;
import org.DitaSemia.Base.ExtensionFunctions.ExtractTextCall;
import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.OxyXPathHandler;
import org.DitaSemia.Oxygen.OxyXPathHandler.Argument;
import org.DitaSemia.Oxygen.OxyXPathHandler.ArgumentType;
import org.apache.log4j.Logger;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;


public class ExtractText implements OxyXPathHandler.CustomFunction {

	private static final Logger logger = Logger.getLogger(ExtractText.class.getName());

	private static final String	ERR_TEXT	= "'<ERR>'";
	
	private final XslTransformerCache 	transformerCache;
	private final SaxonDocumentBuilder	documentBuilder;
	private final Configuration			configuration;
	private final XPathCache			xPathCache;
	
	public ExtractText(XslTransformerCache transformerCache, SaxonDocumentBuilder documentBuilder) {
		this.transformerCache 	= transformerCache;
		this.documentBuilder	= documentBuilder;
		
		configuration	= transformerCache.getConfiguration();
		xPathCache		= new XPathCache(configuration);
	}
	
	@Override
	public String getName() {
		return DitaUtil.NAMESPACE_PREFIX + ":extractText";
	}

	@Override
	public String evaluate(List<Argument> arguments, AuthorNodeWrapper context) {
		
		logger.info("evaluate: " + arguments.get(0));
		
		try {
			final XsltExecutable 	executable 			= transformerCache.getExecutable(ExtractTextCall.SCRIPT_URI);
			final XsltTransformer	transformer			= executable.load();
			final StringWriter		result				= new StringWriter();
			final Processor			processor			= new Processor(configuration);
			final Serializer		serializer			= processor.newSerializer(result);
			final AuthorNodeWrapper	contextAuthorNode	= arguments.get(0).node;
			final String 			xPathToElement 		= XsltConref.createXPathToElement(contextAuthorNode);
			final Source 			xmlSource 			= configuration.getURIResolver().resolve(contextAuthorNode.getBaseUrl().toString(), "");
			final XdmNode 			contextDocNode		= documentBuilder.build(xmlSource, true, true);
			final XPathSelector  	xPathSel			= xPathCache.getXPathSelector(xPathToElement, contextDocNode);
			final XdmNode 			contextNode 		= (XdmNode)xPathSel.evaluateSingle();
			logger.info("contextDocNode: " + contextDocNode);
			logger.info("contextNode: " + contextNode);

			if (contextNode == null) {
				return "";
			} else {
				transformer.setInitialContextNode(contextNode);
				transformer.setInitialMode(new QName(ExtractTextCall.INITIAL_MODE));
				
				serializer.setOutputProperty(Serializer.Property.METHOD, "text");
				transformer.setDestination(serializer);
				
				transformer.transform();
				logger.info("result: " + result.toString());
				return "'" + result.toString() + "'";
			}
		} catch(Exception e) {
			logger.error(e, e);
			return ERR_TEXT;
		}
	}

	@Override
	public ArgumentType[] getArgumentTypes() {
		final ArgumentType[] argumentTypes = {ArgumentType.NODE};
		return argumentTypes;
	}
}
