/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Base.XsltConref;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;

import org.DitaSemia.Base.EmbeddedXPathResolver;
import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.XPathCache;
import org.DitaSemia.Base.XslTransformerCache;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

public class XsltConref {
	
	private static final Logger logger = Logger.getLogger(XsltConref.class.getName());
	
	public static final String 	ATTR_URI 					= "xsl";
	public static final String 	ATTR_XML_SOURCE_URI			= "source";
	public static final String 	ATTR_START_TEMPLATE			= "start-template";
	public static final String 	ATTR_REPARSE				= "reparse";
	
	public static final String	YES							= "yes";
	
	public static final String 	PARAM_CURRENT				= "current";
	public static final String 	NAME_NO_CONTENT				= "no-content";
	public static final String 	NAMESPACE_PARAMETER_URI		= "http://www.dita-semia.org/xslt-conref/custom-parameter";
	public static final String 	NAMESPACE_URI				= "http://www.dita-semia.org/xslt-conref";
	public static final String 	NAMESPACE_PREFIX			= "xcr";

	public static final String 	CONFIG_FILE_URL 			= "/cfg/xslt-conref-saxon-config.xml";

	protected final NodeWrapper 			node;
	protected final XslTransformerCache 	transformerCache;
	protected final XPathCache 				xPathCache;
	protected 		URL 					baseUri;				

	
	public static XsltConref fromNode(NodeWrapper node, XslTransformerCache transformerCache, XPathCache xPathCache)	{
		if (isXsltConref(node)) {
			return new XsltConref(node, transformerCache, xPathCache);
		}
		return null;
	}
	
	public static Configuration loadBaseConfiguration() throws XPathException {
		Configuration 	configuration	= null;
		URL				configFileUrl	= null;
		try {
			configFileUrl = XsltConref.class.getResource(CONFIG_FILE_URL);
			configuration = Configuration.readConfiguration(new SAXSource(new InputSource(configFileUrl.toExternalForm())));
		} catch (Exception e) {
			throw new XPathException("failed to load saxon configuration file (" + CONFIG_FILE_URL + "): " + e.getMessage());
		}
		return configuration;
	}
	
	public static Configuration createConfiguration() throws XPathException {
		final Configuration 	configuration	= loadBaseConfiguration();
		// TODO: register functions
		return configuration;
	}
	
	
	public static boolean isXsltConref(NodeWrapper node) {
		boolean isXsltConref = ((node != null) && 
								(node.isElement()) && 
								(node.getAttribute(XsltConref.ATTR_URI, NAMESPACE_URI) != null) &&
								(!node.getAttribute(XsltConref.ATTR_URI, NAMESPACE_URI).isEmpty()));
		return isXsltConref;
	}
	
	
	private XsltConref(NodeWrapper node, XslTransformerCache transformerCache, XPathCache xPathCache) {
		this.node 				= node;
		this.transformerCache	= transformerCache;
		this.xPathCache			= xPathCache;
		this.baseUri			= node.getBaseUri();
	}
	
	
	public void setBaseUri(final URL baseUri) {
		this.baseUri = baseUri;
	}
	
	
	public NodeInfo resolve() throws XPathException {
		//logger.info("resolve()");
		final URL 				scriptUrl 		= getScriptUrl();
		final XsltExecutable 	xsltExecutable 	= transformerCache.getExecutable(scriptUrl, node.getUriResolver());
		final XsltTransformer 	xsltTransformer = xsltExecutable.load();
		//logger.info("xsltTransformer: " + xsltTransformer);

		
		if (getStartTemplate() != null) {
			try {
				xsltTransformer.setInitialTemplate(new QName(getStartTemplate()));
			} catch (SaxonApiException e) {
				//TODO
				throw new XPathException(e.getMessage());
			}
		} else {
			Source 	xmlSource 	= getXmlSource();
			if ((xmlSource == null) && (node instanceof SaxonNodeWrapper)) {
				//logger.info("Source is root.");
				
				// use current root document as input
				final NodeInfo nodeInfo = ((SaxonNodeWrapper)node).getNodeInfo();
				xsltTransformer.setInitialContextNode(new XdmNode(nodeInfo.getRoot()));
				
				// set "xcr:current"
				xsltTransformer.setParameter(new QName(NAMESPACE_PREFIX, NAMESPACE_URI, PARAM_CURRENT), new XdmNode(nodeInfo));
				
			} else {

				boolean xmlSourceIsBaseUri	= false;
				if (xmlSource == null) {
					//logger.info("Source is base uri.");
					
					// 	use current base uri as input
					final String baseUri = node.getBaseUri().toExternalForm();
					try {
						xmlSource 			= node.getUriResolver().resolve(baseUri, "");;
						xmlSourceIsBaseUri 	= true;
					} catch (TransformerException e) {
						throw new XPathException("Error resolving the source URL: '" + baseUri + "': " + e.getMessage());
					}
				} else if (!FileUtil.fileExists(xmlSource.getSystemId())) {
					// dedicated error message for this scenario
					throw new XPathException("Input source could not be found. (URL: '" + xmlSource.getSystemId() + "')");
				}
				
				// remove the provided xml reader to force saxon creating its own one using the configuration and, thus, expanding the attribute defaults
				if (xmlSource instanceof SAXSource) {
					((SAXSource)xmlSource).setXMLReader(null);
				}
				
				try {
					final Processor 		processor 	= new Processor(transformerCache.getConfiguration());
					final DocumentBuilder 	builder 	= processor.newDocumentBuilder();
					final XdmNode 			context 	= builder.build(xmlSource);
					
					if (xmlSourceIsBaseUri) {
						// set "xcr:current"
						final String 			xPathString	= createXPathToElement(node);
						final XPathExecutable 	xPathExe 	= xPathCache.getXPathExecutable(xPathString);
						final XPathSelector  	xPathSel	= xPathExe.load();
						try {
							xPathSel.setContextItem(context);
							xsltTransformer.setParameter(new QName(NAMESPACE_PREFIX, NAMESPACE_URI, PARAM_CURRENT), xPathSel.evaluateSingle());
						} catch (SaxonApiException e) {
							throw new XPathException(e.getMessage(), e);
						}
					}
					
					xsltTransformer.setInitialContextNode(context);
				} catch (SaxonApiException e) {
					throw new XPathException("Error reading input source ('" + node.getAttribute(ATTR_XML_SOURCE_URI, NAMESPACE_URI) + "'): " + e.getMessage());
				}
			}
		}

		setCustomParameters(xsltTransformer);

		//logger.info("scriptUrl: " + scriptUrl);
		try {
			
			final XdmDestination destination = new XdmDestination();
			xsltTransformer.setDestination(destination);
			xsltTransformer.transform();
			
			return destination.getXdmNode().getUnderlyingNode();
			
		} catch (SaxonApiException e) {
			throw new XPathException("Runtime Error. " + e.getMessage());
		} 
	}

	
	public URL getScriptUrl() {
		final URIResolver uriResolver = node.getUriResolver();
		try {
			return new URL(uriResolver.resolve(node.getAttribute(ATTR_URI, NAMESPACE_URI), baseUri.toExternalForm()).getSystemId());
		} catch (TransformerException | MalformedURLException e) {
			logger.error(e, e);
		}
		return null;
	}
	
	
	public String getScriptUrlAsString() {
		final URL scriptUrl = getScriptUrl();
		return (scriptUrl == null) ? null : scriptUrl.toExternalForm();
	}

	public String getScriptName() {
		return node.getAttribute(ATTR_URI, NAMESPACE_URI);
	}
	
	
	public Source getXmlSource() {
		final String attrValue = node.getAttribute(ATTR_XML_SOURCE_URI, NAMESPACE_URI);
		if ((attrValue != null) && (!attrValue.isEmpty())) {
			final URIResolver uriResolver = node.getUriResolver();
			try {
				return uriResolver.resolve(attrValue, baseUri.toExternalForm());
			} catch (TransformerException e) {
				logger.error(e, e);
			}
		}
		return null;
	}
	
	
	public URL getXmlSourceUrl() {
		final Source source = getXmlSource();
		if (source != null) {
			try {
				return new URL(source.getSystemId());
			} catch (MalformedURLException e) {
				logger.error(e, e);
			}
		}
		return null;
	}
	

	public String getStartTemplate() {
		return node.getAttribute(ATTR_START_TEMPLATE, NAMESPACE_URI);
	}
	
	public static boolean getReparse(final NodeInfo resolvedElement) {
		final String reparseString = resolvedElement.getAttributeValue(NAMESPACE_URI, ATTR_REPARSE);
		//logger.info("reparseString: " + reparseString);
		return ((reparseString != null) && (reparseString.equals(YES)));
	}
	
	private void setCustomParameters(XsltTransformer xsltTransformer) throws XPathException {
		//logger.info("setCustomParameters()");
		final URL 				scriptUrl 		= getScriptUrl();
		final XsltExecutable 	xsltExecutable 	= transformerCache.getExecutable(scriptUrl, node.getUriResolver());
		final List<String> attrNameList = node.getAttributeNamesOfNamespace(NAMESPACE_PARAMETER_URI);
		for (String attrName : attrNameList) {
			//logger.info("attribute: " + attrName);
			final QName 	paramName 	= new QName(attrName.replaceAll("(^[^\\{\\}]*:)|(^\\{.*\\})", ""));
			final String 	paramValue	= node.getAttribute(attrName, NAMESPACE_PARAMETER_URI);
			//logger.info("set custom parameter: " + paramName + " = '" + paramValue + "'");
			if (paramValue != null) {
				if (xsltExecutable.getGlobalParameters().containsKey(paramName)) {
					try {
						xsltTransformer.setParameter(paramName, new XdmAtomicValue(EmbeddedXPathResolver.resolve(paramValue, node), ItemType.UNTYPED_ATOMIC));
						//logger.info("parameters set. ");
					} catch (SaxonApiException e) {
						logger.error(e, e);
					}
				} else {
					//logger.error("Parameter '" + paramName + "' not defined in script.");
				}
			}
		}
	}
	
	private static String createXPathToElement(NodeWrapper node) {
		final URL baseUri = node.getBaseUri();
		String createXPathToElement = "";
		
		while ((node != null) && (node.getParent() != null) && (node.getParent().getBaseUri().equals(baseUri))) {
			createXPathToElement = "/*[" + node.getChildIndexWithinParent() + "]" + createXPathToElement;
			node = node.getParent();
		}
		
		//logger.info("createXPathToElement: result = " + createXPathToElement);
		return createXPathToElement;
	}

}
