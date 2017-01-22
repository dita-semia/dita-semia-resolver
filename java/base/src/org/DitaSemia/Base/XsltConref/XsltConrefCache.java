package org.DitaSemia.Base.XsltConref;

import javax.xml.transform.URIResolver;

import net.sf.saxon.Configuration;

import org.DitaSemia.Base.ConfigurationInitializer;
import org.DitaSemia.Base.Log4jErrorListener;
import org.DitaSemia.Base.SaxonDocumentBuilder;
import org.DitaSemia.Base.XPathCache;
import org.DitaSemia.Base.XslTransformerCache;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetKeyDefRootByRefStringDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetKeyDefRootDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetMatchingKeyDefsDef;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.DitaSemia.Base.DocumentCaching.GetAncestorPathDef;
import org.DitaSemia.Base.DocumentCaching.GetChildTopicsDef;
import org.apache.log4j.Logger;

public class XsltConrefCache {
	
	private static final Logger logger = Logger.getLogger(XsltConrefCache.class.getName());

	public static final String 	CONFIG_FILE_URL 			= "/cfg/xslt-conref-saxon-config.xml";
	
    
	protected Configuration			configuration;
	protected XslTransformerCache	transformerCache;
	protected XPathCache			xPathCache;
	protected SaxonDocumentBuilder	documentBuilder;
	
	public XsltConrefCache(BookCacheProvider bookCacheProvider, ConfigurationInitializer configurationInitializer, SaxonDocumentBuilder	documentBuilder) {
		configuration 		= createConfiguration(bookCacheProvider);
		
		if (configurationInitializer != null) {
			configurationInitializer.initConfig(configuration);
		}

		transformerCache		= new XslTransformerCache(configuration);
		xPathCache				= new XPathCache(configuration);
		this.documentBuilder	= documentBuilder;
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}

	public static Configuration createConfiguration(BookCacheProvider bookCacheProvider) {
		final Configuration 	configuration	= SaxonDocumentBuilder.loadConfiguration(XsltConref.class.getResource(CONFIG_FILE_URL));
		configuration.setErrorListener(new Log4jErrorListener(logger));

		configuration.registerExtensionFunction(new GetChildTopicsDef(bookCacheProvider));
		configuration.registerExtensionFunction(new GetAncestorPathDef(bookCacheProvider));
		configuration.registerExtensionFunction(new GetKeyDefRootDef(bookCacheProvider));
		configuration.registerExtensionFunction(new GetMatchingKeyDefsDef(bookCacheProvider));
		configuration.registerExtensionFunction(new GetKeyDefRootByRefStringDef(bookCacheProvider));
		
		return configuration;
	}

	public XslTransformerCache getTransformerCache() {
		return transformerCache;
	}

	public XPathCache getXPathCache() {
		return xPathCache;
	}

	public SaxonDocumentBuilder getDocumentBuilder() {
		return documentBuilder;
	}

	public URIResolver getUriResolver() {
		return configuration.getURIResolver();
	}
	
	public void clear() {
		transformerCache.clear();
		xPathCache.clear();
	}
}
