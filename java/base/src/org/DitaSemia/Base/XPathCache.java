package org.DitaSemia.Base;

import java.util.HashMap;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaComponent;
import net.sf.saxon.type.Untyped;

import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyRefInterface;
import org.DitaSemia.Base.XsltConref.XsltConref;
import org.apache.log4j.Logger;

public class XPathCache {

	private static final Logger logger = Logger.getLogger(XPathCache.class.getName());

	protected final Configuration 	configuration;
	protected final XPathCompiler 	compiler;
	
	protected HashMap<String, XPathExecutable> 	xPathMapSchemaAware 	= new HashMap<>();
	protected HashMap<String, XPathExecutable> 	xPathMapNonSchemaAware 	= new HashMap<>();
	
	
	public XPathCache(Configuration configuration) {
		this.configuration 	= configuration;
		
		final Processor processor 	= new Processor(configuration);
		compiler = processor.newXPathCompiler();

		declareNamespace(KeyRefInterface.NAMESPACE_PREFIX, 		KeyRefInterface.NAMESPACE_URI);
		declareNamespace(KeyDefInterface.NAMESPACE_PREFIX, 		KeyDefInterface.NAMESPACE_URI);
		declareNamespace(XsltConref.NAMESPACE_PREFIX, 			XsltConref.NAMESPACE_URI);
		declareNamespace(XsltConref.NAMESPACE_PARAMETER_PREFIX, XsltConref.NAMESPACE_PARAMETER_URI);
	}

	
	
	public XPathSelector getXPathSelector(String xPath, XdmNode context) throws XPathException, SaxonApiException {
		final boolean			schemaAware = (context.getUnderlyingNode().getSchemaType() != Untyped.getInstance());
		final XPathExecutable	executable	= getXPathExecutable(xPath, schemaAware);
		final XPathSelector		selector	= executable.load();
		selector.setContextItem(context);
		return selector;
	}

	private XPathExecutable getXPathExecutable(String xPath, boolean schemaAware) throws XPathException {
		final HashMap<String, XPathExecutable> 	xPathMap = (schemaAware) ? xPathMapSchemaAware: xPathMapNonSchemaAware;
		XPathExecutable xPathExecutable = xPathMap.get(xPath);
		if (xPathExecutable == null) {
			try {
				compiler.setSchemaAware(schemaAware);
				xPathExecutable = compiler.compile(xPath);
				xPathMap.put(xPath, xPathExecutable);			
			} catch (SaxonApiException e) {
				logger.error(e.getMessage(), e);
				throw new XPathException("XPath compilation error. (XPath Expression: '" + xPath + "'): " + e.getMessage());
			}
		}
		
		return xPathExecutable;
	}

	
	public void declareNamespace(String prefix, String uri) {
		compiler.declareNamespace(prefix, uri);
	}
	
	public boolean isCompatible(Configuration config) {
		if (this.configuration.isCompatible(config)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void clear() {
		xPathMapSchemaAware.clear();
		xPathMapNonSchemaAware.clear();
	}

}
