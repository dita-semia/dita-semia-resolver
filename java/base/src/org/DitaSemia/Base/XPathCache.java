package org.DitaSemia.Base;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;

import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyRefInterface;
import org.DitaSemia.Base.XsltConref.XsltConref;
import org.apache.log4j.Logger;

public class XPathCache {

	private static final Logger logger = Logger.getLogger(XPathCache.class.getName());

	protected final Configuration 	configuration;
	protected final Processor 		processor;
	protected final XPathCompiler	defaultCompiler;
	
	protected Map<String, XPathExecutable> 	xPathMapSchemaAware 	= new HashMap<>();
	protected Map<String, XPathExecutable> 	xPathMapNonSchemaAware 	= new HashMap<>();
	protected Map<String, String>			namespaceMap			= new HashMap<>();
	
	
	public XPathCache(Configuration configuration) {
		this.configuration 	= configuration;
		
		processor 		= new Processor(configuration);
		defaultCompiler	= processor.newXPathCompiler();

		declareNamespace(DitaUtil.NAMESPACE_PREFIX, 			DitaUtil.NAMESPACE_URI);
		declareNamespace(ConbatResolver.NAMESPACE_PREFIX, 		ConbatResolver.NAMESPACE_URI);
		declareNamespace(KeyRefInterface.NAMESPACE_PREFIX, 		KeyRefInterface.NAMESPACE_URI);
		declareNamespace(KeyDefInterface.NAMESPACE_PREFIX, 		KeyDefInterface.NAMESPACE_URI);
		declareNamespace(XsltConref.NAMESPACE_PREFIX, 			XsltConref.NAMESPACE_URI);
		declareNamespace(XsltConref.NAMESPACE_PARAMETER_PREFIX, XsltConref.NAMESPACE_PARAMETER_URI);
	}

	
	public XPathSelector getXPathSelector(String xPath, XdmNode context) throws XPathException, SaxonApiException {
		return getXPathSelector(xPath, context, null);
	}
	
	public XPathSelector getXPathSelector(String xPath, XdmNode context, Map<QName, XdmValue> variableMap) throws XPathException, SaxonApiException {
		final SchemaType	schemaType 	= context.getUnderlyingNode().getSchemaType();
		final boolean		schemaAware = ((schemaType != Untyped.getInstance()) && (schemaType != BuiltInAtomicType.UNTYPED_ATOMIC));

		final XPathExecutable	executable	= getXPathExecutable(xPath, schemaAware, variableMap);
		final XPathSelector		selector	= executable.load();
		selector.setContextItem(context);

		if (variableMap != null) {
			for (Entry<QName, XdmValue> entry: variableMap.entrySet()) {
				selector.setVariable(entry.getKey(), entry.getValue());
			}
		}
		return selector;
	}

	private XPathExecutable getXPathExecutable(String xPath, boolean schemaAware, Map<QName, XdmValue> variableMap) throws XPathException {
		final Map<String, XPathExecutable> 	xPathMap = (schemaAware) ? xPathMapSchemaAware: xPathMapNonSchemaAware;
		XPathExecutable xPathExecutable = xPathMap.get(xPath);
		if (xPathExecutable == null) {
			try {
				XPathCompiler compiler;
				if (variableMap != null) {
					compiler = processor.newXPathCompiler();
					for (Entry<String, String> entry: namespaceMap.entrySet()) {
						compiler.declareNamespace(entry.getKey(), entry.getValue());	
					}
					for (QName name : variableMap.keySet()) {
						compiler.declareVariable(name);
						//logger.info("declare variable: " + name);
					}
				} else {
					compiler = defaultCompiler;
				}
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
		namespaceMap.put(prefix, uri);
		defaultCompiler.declareNamespace(prefix, uri);
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
