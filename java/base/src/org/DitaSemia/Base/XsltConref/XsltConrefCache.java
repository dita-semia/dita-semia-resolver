package org.DitaSemia.Base.XsltConref;

import javax.xml.transform.URIResolver;

import net.sf.saxon.Configuration;

import org.DitaSemia.Base.ConfigurationInitializer;
import org.DitaSemia.Base.Log4jErrorHandler;
import org.DitaSemia.Base.Log4jErrorListener;
import org.DitaSemia.Base.SaxonCachedDocumentBuilder;
import org.DitaSemia.Base.SaxonConfigurationFactory;
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
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XsltConrefCache {
	
	private static final Logger logger = Logger.getLogger(XsltConrefCache.class.getName());

	public static final String 	CONFIG_FILE_URL 			= "/cfg/xslt-conref-saxon-config.xml";
	
    public static final String FEATURE_NAMESPACE 			= "http://xml.org/sax/features/namespaces";
    public static final String FEATURE_VALIDATION 			= "http://xml.org/sax/features/validation";
    public static final String FEATURE_VALIDATION_SCHEMA 	= "http://apache.org/xml/features/validation/schema";
    
	protected Configuration			configuration;
	protected XslTransformerCache	transformerCache;
	protected XPathCache			xPathCache;
	protected XMLReader				xmlReader;
	protected SaxonDocumentBuilder	documentBuilder;
	
	public XsltConrefCache(BookCacheProvider bookCacheProvider, ConfigurationInitializer configurationInitializer) {
		configuration 		= createConfiguration(bookCacheProvider);
		
		if (configurationInitializer != null) {
			configurationInitializer.initConfig(configuration);
		}

		transformerCache	= new XslTransformerCache(configuration);
		xPathCache			= new XPathCache(configuration);
		try {
			xmlReader			= XMLReaderFactory.createXMLReader();

			xmlReader.setFeature(FEATURE_VALIDATION, 			true);
			xmlReader.setFeature(FEATURE_VALIDATION_SCHEMA, 	true);
			xmlReader.setFeature(FEATURE_NAMESPACE, 			true);
			xmlReader.setErrorHandler(new Log4jErrorHandler(logger));
			
			if (configuration.getURIResolver() instanceof EntityResolver) {
				xmlReader.setEntityResolver((EntityResolver) configuration.getURIResolver());
				// TODO: find a better (this won't work when reparsing an xslt-conref while filling the cache in oXygen
			}
		} catch (SAXException e) {
			logger.error("ERROR initializing XML-Reader: " + e, e);
			xmlReader = null;
		}
		documentBuilder = new SaxonCachedDocumentBuilder(new SaxonConfigurationFactory() {
			@Override
			public Configuration createConfiguration() {
				return loadConfiguration(XsltConref.class.getResource(CONFIG_FILE_URL));
			}
		});
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}

	public static Configuration createConfiguration(BookCacheProvider bookCacheProvider) {
		final Configuration 	configuration	= SaxonConfigurationFactory.loadConfiguration(XsltConref.class.getResource(CONFIG_FILE_URL));
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

	public XMLReader getXmlReader() {
		return xmlReader;
	}

	public SaxonDocumentBuilder getDocumentBuilder() {
		return documentBuilder;
	}

	public URIResolver getUriResolver() {
		return configuration.getURIResolver();
	}
}
