package org.DitaSemia.Base.XsltConref;

import java.net.URL;

import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Initializer;

import org.DitaSemia.Base.Log4jErrorListener;
import org.DitaSemia.Base.SaxonDocumentBuilder;
import org.DitaSemia.Base.XPathCache;
import org.DitaSemia.Base.XslTransformerCache;
import org.DitaSemia.Base.XslTransformerCacheProvider;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetTypeNameDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.AncestorPathDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetAncestorKeyDefDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetDisplaySuffixDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetIsDontLinkDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetIsFilteredKeyDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetIsKeyHiddenDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetIsOverwritableDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetIsResourceOnlyDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetKeyDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetKeyDefByRefStringDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetKeyDefByTypeNameDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetKeyDefFromNodeDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetKeyFilterAttrDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetKeyTypeDefDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetLocationDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetMatchingKeyDefsDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetNameDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetPathDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetRefStringDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetRootDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetTypeDefDef;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.DitaSemia.Base.ExtensionFunctions.EvaluateXPathDef;
import org.DitaSemia.Base.ExtensionFunctions.ExecuteXsltDef;
import org.DitaSemia.Base.ExtensionFunctions.ExtractContentTextDef;
import org.DitaSemia.Base.ExtensionFunctions.ExtractTextDef;
import org.DitaSemia.Base.ExtensionFunctions.GetChildTopicsDef;
import org.DitaSemia.Base.ExtensionFunctions.GetElementByHrefDef;
import org.DitaSemia.Base.ExtensionFunctions.GetTextWidthDef;
import org.DitaSemia.Base.ExtensionFunctions.GetTopicNumDef;
import org.DitaSemia.Base.ExtensionFunctions.HyphenateWordDef;
import org.DitaSemia.Base.ExtensionFunctions.LoadXmlFileDef;
import org.DitaSemia.Base.ExtensionFunctions.ResolveEmbeddedXPathDef;
import org.apache.log4j.Logger;

public class XsltConrefCache implements XslTransformerCacheProvider {
	
	private static final Logger logger = Logger.getLogger(XsltConrefCache.class.getName());

	public static final String 	CONFIG_FILE_URL 			= "/cfg/xslt-conref-saxon-config.xml";
    
	protected final BookCacheProvider		bookCacheProvider;
	protected final Configuration			configuration;
	protected final XslTransformerCache		transformerCache;
	protected final XPathCache				xPathCache;
	protected final SaxonDocumentBuilder	documentBuilder;
	protected final String					basedir;
	
	public XsltConrefCache(BookCacheProvider bookCacheProvider, Initializer configurationInitializer, SaxonDocumentBuilder	documentBuilder, URL configUrl, String basedir) {
		configuration = createConfiguration(
				configUrl == null ? XsltConrefCache.class.getResource(CONFIG_FILE_URL) : configUrl, 
				bookCacheProvider, 
				this, 
				documentBuilder);
		
		if (configurationInitializer != null) {
			try {
				configurationInitializer.initialize(configuration);
			} catch (TransformerException e) {
				logger.error(e, e);
			}
		}

		transformerCache		= new XslTransformerCache(configuration);
		xPathCache				= new XPathCache(configuration);
		this.bookCacheProvider	= bookCacheProvider;
		this.documentBuilder	= documentBuilder;
		this.basedir			= (basedir != null) ? basedir.replace('\\',  '/') : null;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public static Configuration createConfiguration(
			URL 						configUrl, 
			BookCacheProvider 			bookCacheProvider, 
			XslTransformerCacheProvider xslTransformerCacheProvider, 
			SaxonDocumentBuilder		documentBuilder) {
		
		final Configuration 	configuration	= SaxonDocumentBuilder.loadConfiguration(configUrl);
		configuration.setErrorListener(new Log4jErrorListener(logger));

		registerExtensionFunctions(configuration, bookCacheProvider, xslTransformerCacheProvider, documentBuilder);
		
		return configuration;
	}
	
	public static void registerExtensionFunctions(
			Configuration 				configuration,
			BookCacheProvider 			bookCacheProvider, 
			XslTransformerCacheProvider xslTransformerCacheProvider, 
			SaxonDocumentBuilder		documentBuilder) {

		// dita-semia
		configuration.registerExtensionFunction(new GetChildTopicsDef(bookCacheProvider));
		configuration.registerExtensionFunction(new GetTopicNumDef(bookCacheProvider));
		configuration.registerExtensionFunction(new GetTextWidthDef());
		configuration.registerExtensionFunction(new HyphenateWordDef());
		configuration.registerExtensionFunction(new EvaluateXPathDef());
		configuration.registerExtensionFunction(new ResolveEmbeddedXPathDef());
		configuration.registerExtensionFunction(new ExecuteXsltDef(documentBuilder));
		configuration.registerExtensionFunction(new LoadXmlFileDef(documentBuilder));
		configuration.registerExtensionFunction(new GetElementByHrefDef(bookCacheProvider));
		configuration.registerExtensionFunction(new ExtractTextDef(xslTransformerCacheProvider));
		configuration.registerExtensionFunction(new ExtractContentTextDef(xslTransformerCacheProvider));
		
		// advanced-keyref
		configuration.registerExtensionFunction(new GetAncestorKeyDefDef(bookCacheProvider));
		configuration.registerExtensionFunction(new GetMatchingKeyDefsDef(bookCacheProvider));
		configuration.registerExtensionFunction(new GetKeyDefByRefStringDef(bookCacheProvider));
		//configuration.registerExtensionFunction(new GetKeyDefFromNodeDef(bookCacheProvider));
		configuration.registerExtensionFunction(new GetKeyTypeDefDef(bookCacheProvider));
		configuration.registerExtensionFunction(new GetDisplaySuffixDef(bookCacheProvider));
		
		// dynamic-xml-definition
		configuration.registerExtensionFunction(new GetKeyDefByTypeNameDef(bookCacheProvider));
		configuration.registerExtensionFunction(new GetTypeDefDef(bookCacheProvider, xslTransformerCacheProvider));
		configuration.registerExtensionFunction(new GetTypeNameDef());
		
		// implicit-keydef
		configuration.registerExtensionFunction(new GetRefStringDef());
		configuration.registerExtensionFunction(new GetKeyDef());
		configuration.registerExtensionFunction(new GetNameDef());
		configuration.registerExtensionFunction(new GetPathDef());
		configuration.registerExtensionFunction(new GetRootDef(bookCacheProvider));
		configuration.registerExtensionFunction(new GetIsDontLinkDef());
		configuration.registerExtensionFunction(new GetIsFilteredKeyDef());
		configuration.registerExtensionFunction(new GetIsKeyHiddenDef());
		configuration.registerExtensionFunction(new GetIsOverwritableDef());
		configuration.registerExtensionFunction(new GetIsResourceOnlyDef());
		configuration.registerExtensionFunction(new GetLocationDef());
		configuration.registerExtensionFunction(new GetKeyFilterAttrDef());
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

	@Override
	public XslTransformerCache getXslTransformerCache() {
		return transformerCache;
	}

	public BookCacheProvider getBookCacheProvider() {
		return bookCacheProvider;
	}

	public String getBaseDir() {
		return basedir;
	}
}
