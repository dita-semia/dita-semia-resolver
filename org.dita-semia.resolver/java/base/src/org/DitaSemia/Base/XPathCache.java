package org.DitaSemia.Base;

import java.util.HashMap;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.trans.XPathException;

import org.apache.log4j.Logger;

public class XPathCache {

	private static final Logger logger = Logger.getLogger(XPathCache.class.getName());

	protected final Configuration 	configuration;
	protected final Processor 		processor;
	protected final XPathCompiler 	compiler;
	
	protected HashMap<String, XPathExecutable> 	xPathMap 	= new HashMap<>();
	
	
	public XPathCache(Configuration configuration) {
		this.configuration 	= configuration;
		
		processor 	= new Processor(configuration);
		compiler 	= processor.newXPathCompiler();
	}
	
	
	public XPathExecutable getXPathExecutable(String xPath) throws XPathException {
		
		XPathExecutable xPathExecutable = xPathMap.get(xPath);
		if (xPathExecutable == null) {
			try {				
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

}
