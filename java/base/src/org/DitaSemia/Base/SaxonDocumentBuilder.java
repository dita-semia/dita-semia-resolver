package org.DitaSemia.Base;


import java.io.File;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.apache.log4j.Logger;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.tree.util.DocumentNumberAllocator;

public class SaxonDocumentBuilder {

	private static final Logger logger = Logger.getLogger(SaxonDocumentBuilder.class.getName());


    public static final String FEATURE_NAMESPACE 			= "http://xml.org/sax/features/namespaces";
    public static final String FEATURE_VALIDATION 			= "http://xml.org/sax/features/validation";
    public static final String FEATURE_VALIDATION_SCHEMA 	= "http://apache.org/xml/features/validation/schema";
    public static final String PROPERTY_GRAMMAR_POOL		= "http://apache.org/xml/properties/internal/grammar-pool";


	protected final static NamePool					namePool 				= new NamePool();
	protected final static DocumentNumberAllocator	documentNumberAllocator	= new DocumentNumberAllocator();
	
	
	protected final EntityResolver 	entityResolver;
	protected final URIResolver 	uriResolver;
	
	protected final Configuration 	configuration;
	protected final DocumentBuilder documentBuilder;
	
	protected final XMLGrammarPool	xmlGrammarPool	= new XMLGrammarPoolImplDS();;
    
    public final Map<String, NodeWithTimestamp> nodeCache	= new HashMap<>();
    
	public SaxonDocumentBuilder(EntityResolver entityResolver, URIResolver uriResolver) {
		this.entityResolver	= entityResolver;
		this.uriResolver	= uriResolver;
		
		configuration = new Configuration();
		configuration.setURIResolver(uriResolver);
		makeConfigurationCompatible(configuration);

		final Processor processor = new Processor(configuration);
		documentBuilder = processor.newDocumentBuilder();
	}
	
	public SaxonDocumentBuilder(EntityResolver entityResolver, Configuration configuration) {
		this.entityResolver	= entityResolver;
		this.uriResolver	= configuration.getURIResolver();
		this.configuration	= configuration;

		final Processor processor = new Processor(configuration);
		documentBuilder = processor.newDocumentBuilder();
	}

	public XdmNode build(Source source, boolean expandAttributeDefaults, boolean useCache) throws SaxonApiException{
		final String systemId	= source.getSystemId();
		long 	timestamp 		= -1;
		XdmNode result			= null;
		
		if (source instanceof SAXSource) {
			final SAXSource saxSource = ((SAXSource)source);
			
			// check if cachable
			InputSource inputSource = saxSource.getInputSource();
			if ((useCache) && (inputSource.getCharacterStream() == null) && (inputSource.getByteStream() == null)) {
				// check timestamp
				timestamp = FileUtil.getLastModified(systemId);
				//logger.info("url: " + source.getSystemId() + ", timestamp: " + FileUtil.TIMESTAMP_FORMAT.format(timestamp));
				NodeWithTimestamp cacheEntry = nodeCache.get(source.getSystemId());
				if ((cacheEntry != null) && (cacheEntry.getTimestamp() == timestamp)) {
					result = cacheEntry.getNode();
					//logger.info("from cache: " + source.getSystemId());
				}
			}

			if (result == null) {
				// 	set reader
				saxSource.setXMLReader(getXmlReader(expandAttributeDefaults, useCache));
			}
			//logger.info("CharacterStream: " + inputSource.getCharacterStream());
			//logger.info("ByteStream: " + inputSource.getByteStream());
		}
		
		if (result == null) {
			//logger.info("building document: " + source.getSystemId());
			result = documentBuilder.build(source);
			
			if (timestamp > 0) {
				nodeCache.put(systemId, new NodeWithTimestamp(result, timestamp));
				//logger.info("into cache: " + systemId);
			}
		}
		//logger.info("result (" + source.getSystemId() + ") :");
		//logger.info(SaxonNodeWrapper.serializeNode(result.getUnderlyingNode()));
		return result;
	}

	public XdmNode buildFromString(String sourceString, boolean expandAttributeDefaults, boolean useCache) throws SaxonApiException{
		final SAXSource source = new SAXSource(getXmlReader(expandAttributeDefaults, useCache), new InputSource(new StringReader(sourceString)));
		final XdmNode result = documentBuilder.build(source);
		//logger.info("result from String :");
		//logger.info(SaxonNodeWrapper.serializeNode(result.getUnderlyingNode()));
		return result;
	}

	public void clearCache() {
		nodeCache.clear();
		xmlGrammarPool.clear();
	}
	
	private XMLReader getXmlReader(boolean expandAttributeDefaults, boolean useCache) {
		// always create a new instance to avoid having the schema being bound to the namespace (not the case for DITA)
		try {
			final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setFeature(FEATURE_NAMESPACE, true);
			xmlReader.setErrorHandler(new Log4jErrorHandler(logger));
			xmlReader.setEntityResolver(entityResolver);
			if (expandAttributeDefaults) {
				xmlReader.setFeature(FEATURE_VALIDATION,	 	true);
				xmlReader.setFeature(FEATURE_VALIDATION_SCHEMA,	true);
				if (useCache) {
					xmlReader.setProperty(PROPERTY_GRAMMAR_POOL,		xmlGrammarPool);
				}
			}
			return xmlReader;
		} catch (SAXException e) {
			logger.error("ERROR initializing XML reader: " + e, e);
			return null;
		}
	}
	
	public static Configuration loadConfiguration(URL configUrl) {
		if (configUrl != null) {
			try {
				final Configuration configuration = Configuration.readConfiguration(new SAXSource(new InputSource(configUrl.toExternalForm())));
				makeConfigurationCompatible(configuration);
				return configuration;
			} catch (Exception e) {
				throw new RuntimeException("failed to load saxon configuration file (" + FileUtil.decodeUrl(configUrl) + "): " + e.getMessage());
			}
		} else {
			throw new RuntimeException("can't load configuration file from URL 'null'.");
		}
	}
	
	public static void makeConfigurationCompatible(Configuration configuration) {
		// make compatible with base configuration
		configuration.setNamePool(namePool);
		configuration.setDocumentNumberAllocator(documentNumberAllocator);
	}

	private static class NodeWithTimestamp {
		
		private XdmNode xdmNode;
		private long 	timestamp;
		
		private NodeWithTimestamp(XdmNode xdmNode, long timestamp) {
			this.xdmNode 	= xdmNode;
			this.timestamp	= timestamp;
		}

		private XdmNode getNode() {
			return xdmNode;
		}

		private long getTimestamp() {
			return timestamp;
		}
	}

	public boolean isCompatible(Configuration configuration) {
		return this.configuration.isCompatible(configuration);
	}

	public Configuration getConfiguration() {
		return configuration;
	}
	
}
