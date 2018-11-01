package org.DitaSemia.Oxygen;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import net.sf.saxon.Configuration;
import net.sf.saxon.dom.ElementOverNodeInfo;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.UnfailingErrorListener;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import ro.sync.ecss.extensions.commons.id.GenerateIDElementsInfo;

import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.FilterProperties;
import org.DitaSemia.Base.Href;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.XPathCache;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.FileCache;
import org.apache.log4j.Logger;

public class SchematronUtil {

	private static final Logger logger = Logger.getLogger(SchematronUtil.class.getName());
	
	public static final String THREAD_FULL_MAP_VALIDATION	= "DITA Map Completeness Checker";
	public static final String THREAD_AUTOMATIC_VALIDATION	= "AutomaticallyValidation";
	public static final String THREAD_EXPLICIT_VALIDATION	= "Validator";

	protected static XPathCache 	xPathCache 	= null;
	
	protected static Configuration 	validatingConfiguration = null; 


	public static NodeInfo evaluateXPathToNode(ElementOverNodeInfo currentElement, String xPath) {
		if(xPath != null) {
			try {
				final SaxonNodeWrapper result = (SaxonNodeWrapper)createSaxonNodeWrapper(currentElement).evaluateXPathToNode(xPath);
				if (result != null) {
					return result.getNodeInfo(); 
				} else {
					return null;
				}
			} catch (Exception e) {
				logger.error(e, e);
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static String evaluateXPathToStringList(ElementOverNodeInfo currentElement, String xPath) {
		if(xPath != null) {
			try {
				return String.join(KeyDef.PATH_DELIMITER, createSaxonNodeWrapper(currentElement).evaluateXPathToStringList(xPath));
			} catch (Exception e) {
				logger.error(e, e);
				return null;
			}
		} else {
			return null;
		}
	}


	public static String evaluateXPathToString(ElementOverNodeInfo currentElement, String xPath) {
		if(xPath != null) {
			try {
				return createSaxonNodeWrapper(currentElement).evaluateXPathToString(xPath);
			} catch (Exception e) {
				logger.error(e, e);
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static XmlCache createXmlCache() {
		return new XmlCache();
	}

	public static NodeInfo loadXmlWithValidation(XPathContext context, URL url, XmlCache xmlCache) {
		return xmlCache.loadXmlWithValidation(context, url);
	}
	
	public static String serializeNode(NodeInfo node) {
		return SaxonNodeWrapper.serializeNode(node);
	}

	public static boolean isValidXml(URL url, boolean useCache) {
		try {
			final BookCacheHandler 	bookCacheHandler 	= BookCacheHandler.getInstance();
			final Source 			source 				= bookCacheHandler.getXsltConrefCache().getUriResolver().resolve(url.getFile(), null);
			bookCacheHandler.getDocumentBuilder().build(source, true, useCache);
			return true;
		} catch (SaxonApiException e) {
			return false;
		} catch (Exception e) {
			logger.error(e, e);
			return false;
		}
	}
	
	public static String getFileCacheTimestamp(URL url) {
		final BookCacheHandler 	bookCacheHandler 	= BookCacheHandler.getInstance();
		final BookCache			bookCache			= bookCacheHandler.getBookCache(url);
		final FileCache			fileCache			= bookCache.getFile(url);
		return fileCache.getFileTimestamp();
	}
	
	public static FilterProperties getRootFilterProperites(URL url) {
		if (url != null) { 
			final BookCache	bookCache	= BookCacheHandler.getInstance().getBookCache(url);
			final FileCache	fileCache	= bookCache.getFile(url);
			if (fileCache != null) {
				return fileCache.getRootFilterProperites();
			}
		}
		return null;
	}
	
	public static FilterProperties getFilterPropertiesByHref(String href, URL baseUrl) {
		if ((href != null) && (!href.isEmpty()) && (baseUrl != null)) {
			final Href		hrefObj		= new Href(href, baseUrl);
			//logger.info("url: '" + hrefObj.getRefUrl() + "', id: '" + hrefObj.getRefId() + "'");
			final BookCache	bookCache	= BookCacheHandler.getInstance().getBookCache(baseUrl);
			final FileCache	fileCache	= bookCache.getFile(hrefObj.getRefUrl());
			//logger.info("fileCache: " + fileCache);
			if (fileCache != null) {
				final SaxonNodeWrapper node = fileCache.getElementByRefId(hrefObj.getRefId());
				//logger.info("node: " + node);
				if (node != null) {
					return FilterProperties.getFromNodeWithAncestors(node);
				}
			}
		}
		return null;
	}
	
	public static boolean isFullMapValidation() {
		return Thread.currentThread().getName().equals(THREAD_FULL_MAP_VALIDATION);
	}

	public static boolean ensureAllFilesUpdated(URL url) {
		final BookCache bookCache = BookCacheHandler.getInstance().getBookCache(url);
		bookCache.ensureAllFilesUpdated();
		return true;
	}

	public static boolean ensureAllFilesParsed(URL url) {
		final BookCache bookCache = BookCacheHandler.getInstance().getBookCache(url);
		bookCache.ensureAllFilesParsed();
		return true;
	}

	public static boolean isAllFilesParsed(URL url) {
		final BookCache bookCache = BookCacheHandler.getInstance().getBookCache(url);
		return bookCache.isAllFilesParsed();
	}
	
	public static URI resolveUri(String href, String base) {
		if (href == null) {
			return null;
		} else {
			try {
				final Source source = BookCacheHandler.getInstance().getUriResolver().resolve(href, base);
				return new URI(source.getSystemId());
			} catch (TransformerException | URISyntaxException e) {
				logger.error(e, e);
				return null;
			}
		}
	}
	
	public static Collection<String> getOtherFilesForTopicId(String topicId, URL url) throws XPathException {
		try {
			final BookCache 			bookCache 	= BookCacheHandler.getInstance().getBookCache(url);
			final String				decodedUrl	= FileUtil.decodeUrl(url);
			final String				rootFolder	= bookCache.getRootFile().getDecodedUrl().replaceAll("/[^/]*$", "");
			final Collection<FileCache> files 		= bookCache.getFilesByTopicId(topicId);
			final Collection<String> 	otherFiles 	= new ArrayList<>();
			if (files != null) {
				for (FileCache file : files) {
					final String otherUrl = file.getDecodedUrl();
					if (!otherUrl.equals(decodedUrl)) {
						final String compactUrl = otherUrl.replaceAll(rootFolder, "<map>");
						otherFiles.add(compactUrl);
					}
				}
				return otherFiles;
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException("Error in getOtherFilesForTopicId: " + e.getMessage());
		}
	}

	public static String getUniqueId(URL url) throws XPathException {
		try {
			final BookCache bookCache 	= BookCacheHandler.getInstance().getBookCache(url);
			final FileCache	fileCache 	= bookCache.getFile(url);
			String id;
			do {
				id = GenerateIDElementsInfo.generateID("${id}", "").toUpperCase().replace('_',  '-');
			} while ((bookCache.getFilesByTopicId(id) != null) || (fileCache.getElementByRefId(id) != null));
			return id;
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException("Error in getOtherFilesForTopicId: " + e.getMessage());
		}
	}

	protected static SaxonNodeWrapper createSaxonNodeWrapper(ElementOverNodeInfo currentElement) {
		NodeInfo node = currentElement.getUnderlyingNodeInfo();
		return new SaxonNodeWrapper(node, getXPathCache(currentElement));
	}
	
	protected static XPathCache getXPathCache(ElementOverNodeInfo currentElement) {
		final Configuration elementConfig	= currentElement.getUnderlyingNodeInfo().getConfiguration();
		if ((xPathCache == null) || (!xPathCache.isCompatible(elementConfig))) {
			try {
				final BookCache 	bookCache 	= BookCacheHandler.getInstance().getBookCache(new URL(currentElement.getBaseURI()));
				final Configuration xPathConfig = bookCache.createConfiguration(elementConfig);
				xPathCache = new XPathCache(xPathConfig);
			} catch (MalformedURLException e) {
				logger.error(e, e);
				xPathCache = null;
			}
		}
		return xPathCache;
	}
	
	protected static Configuration getValidatingConfiguration() {
		if (validatingConfiguration == null) {
			validatingConfiguration = OxySaxonConfigurationFactory.createConfiguration(true);
			validatingConfiguration.setValidation(true);
			validatingConfiguration.setSchemaValidationMode(Validation.STRICT);
		}
		validatingConfiguration.clearSchemaCache();	// always make sure the schema is updated
		return validatingConfiguration;
	}
	
	protected static class XmlCache {
		protected Map<String, NodeInfo> map = new HashMap<>();

		public NodeInfo loadXmlWithValidation(XPathContext context, URL url) {
			try {
				final Configuration refConfig	= context.getConfiguration();
				final Source		source 		= refConfig.getURIResolver().resolve(url.getFile(), null); 
				if (map.containsKey(source.getSystemId())) {
					return map.get(source.getSystemId());
				} else {
					final Configuration configuration = getValidatingConfiguration();
					configuration.setNamePool(refConfig.getNamePool());
					configuration.setDocumentNumberAllocator(refConfig.getDocumentNumberAllocator());
					configuration.setErrorListener(new XmlValidationErrorListener(context.getErrorListener()));
					
					NodeInfo result = null;
					try {
						final Processor 		processor 	= new Processor(configuration);
						final DocumentBuilder 	builder 	= processor.newDocumentBuilder();
						final XdmNode 			document	= builder.build(source);
						if (document != null) {
							result = document.getUnderlyingNode();
						}
					} catch (SaxonApiException e) {
						// errors are already reported through error listener
					}	
					
					map.put(source.getSystemId(), result);
					return result;
				}
			} catch (Exception e) {
				logger.error(e, e);
				return null;
			}
		}
	}
	
	protected static class XmlValidationErrorListener implements UnfailingErrorListener {

		protected final UnfailingErrorListener listener;
		
		public XmlValidationErrorListener(UnfailingErrorListener listener) {
			this.listener = listener;
		}
		
		@Override
		public void error(TransformerException exception) {
			//logger.info("error: " + exception + ", " + exception.getClass());
			listener.error(exception);
		}

		@Override
		public void fatalError(TransformerException exception) {
			//logger.info("fatalError: '" + exception.getMessage() + "'");
			if (exception.getMessage().equals("One or more validation errors were reported")) {
				// hide this message
			} else {
				listener.fatalError(exception);
			}
		}

		@Override
		public void warning(TransformerException exception) {
			//logger.info("warning: " + exception + ", " + exception.getClass());
			listener.warning(exception);
		}
		
	}
}
