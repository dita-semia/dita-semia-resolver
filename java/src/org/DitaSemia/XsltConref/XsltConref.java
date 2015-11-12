/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.XsltConref;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;

import org.DitaSemia.JavaBase.FileUtil;
import org.DitaSemia.JavaBase.NodeWrapper;
import org.DitaSemia.JavaBase.SaxonNodeWrapper;
import org.DitaSemia.JavaBase.XslTransformerCache;
import org.apache.log4j.Logger;

public class XsltConref 
{
	//@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(XsltConref.class.getName());
	
	public static final String 	ATTR_URI 					= "xslt-conref";
	public static final String 	ATTR_XML_SOURCE_URI			= "xslt-conref-source";
	public static final String 	ATTR_START_TEMPLATE			= "xslt-conref-start";
	public static final String 	PARAM_XPATH_TO_XSLT_CONREF	= "xPathToXsltConref";
	public static final String 	NAME_NO_CONTENT				= "no-content";
	public static final String 	NAMESPACE_CUSTOM_PARAMETER	= "http://www.dita-semia.org/xslt-conref/custom-parameter";
	
	
	private final NodeWrapper 	node;
	
	public static XsltConref fromNode(NodeWrapper node)	{
		if (isXsltConref(node)) {
			return new XsltConref(node);
		}
		return null;
	}
	
	
	private XsltConref(NodeWrapper node) {
		this.node = node;
	}
	
	
	public static boolean isXsltConref(NodeWrapper node) {
		boolean isXsltConref = ((node != null) && 
								(node.isElement()) && 
								(node.getAttribute(XsltConref.ATTR_URI) != null) &&
								(!node.getAttribute(XsltConref.ATTR_URI).isEmpty()));
		return isXsltConref;
	}
	
	
	public NodeWrapper resolve() throws XPathException {
	
		final URL 				scriptUrl 		= getScriptUrl();
		final XsltTransformer 	xsltTransformer = XslTransformerCache.getInstance().getTransformer(scriptUrl, node.getUriResolver());
//		logger.info("xsltTransformer: " + xsltTransformer);
		final Processor 		processor 		= new Processor(XslTransformerCache.getInstance().getConfiguration());
		
		if (getStartTemplate() != null) {
			try {
				xsltTransformer.setInitialTemplate(new QName(getStartTemplate()));
			} catch (SaxonApiException e) {
				//TODO
				throw new XPathException(e.getMessage());
			}
		} else {
			Source 	xmlSource 	= getXmlSource();
			if (xmlSource == null) {
				// use current document as input
				final String baseUri = node.getBaseUri().toExternalForm();
				try {
					xmlSource = node.getUriResolver().resolve(baseUri, "");
				} catch (TransformerException e) {
					throw new XPathException("Error resolving the source URL: '" + baseUri + "': " + e.getMessage());
				}
				// set specific standard parameter
				xsltTransformer.setParameter(new QName(PARAM_XPATH_TO_XSLT_CONREF), new XdmAtomicValue(createXPathToElement(node)));
			} else if (!FileUtil.fileExists(xmlSource.getSystemId())) {
				// dedicated error message for this scenario
				throw new XPathException("Input source could not be found. (URL: '" + xmlSource.getSystemId() + "')");
			}
			
			// remove the provided xml reader to force saxon creating its own one using the configuration and, thus, expanding the attribute defaults
			if (xmlSource instanceof SAXSource) {
				((SAXSource)xmlSource).setXMLReader(null);
			}
			
			try {
				final DocumentBuilder 	builder = processor.newDocumentBuilder();
				final XdmNode 			context = builder.build(xmlSource);
				xsltTransformer.setInitialContextNode(context);
			} catch (SaxonApiException e) {
				throw new XPathException("Error reading input source ('" + node.getAttribute(ATTR_XML_SOURCE_URI) + "'): " + e.getMessage());
			}
		}

		setCustomParameters(xsltTransformer);

		//logger.info("scriptUrl: " + scriptUrl);
		try {
			final XdmDestination destination = new XdmDestination();
			xsltTransformer.setDestination(destination);
			xsltTransformer.transform();
			return new SaxonNodeWrapper(destination.getXdmNode().getUnderlyingNode());
		} catch (SaxonApiException e) {
			throw new XPathException("Runtime Error. " + e.getMessage());
		} 
	}

	
	public URL getScriptUrl() {
		final URIResolver uriResolver = node.getUriResolver();
		try {
			return new URL(uriResolver.resolve(node.getAttribute(ATTR_URI), node.getBaseUri().toExternalForm()).getSystemId());
		} catch (TransformerException | MalformedURLException e) {
			logger.error(e);
		}
		return null;
	}
	
	public String getScriptUrlAsString() {
		final URL scriptUrl = getScriptUrl();
		return (scriptUrl == null) ? null : scriptUrl.toExternalForm();
	}
	
	
	public Source getXmlSource() {
		final String attrValue = node.getAttribute(ATTR_XML_SOURCE_URI);
		if ((attrValue != null) && (!attrValue.isEmpty())) {
			final URIResolver uriResolver = node.getUriResolver();
			try {
				return uriResolver.resolve(attrValue, node.getBaseUri().toExternalForm());
			} catch (TransformerException e) {
				logger.error(e);
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
				logger.error(e);
			}
		}
		return null;
	}
	

	public String getStartTemplate() {
		return node.getAttribute(ATTR_START_TEMPLATE);
	}
	
	
	private void setCustomParameters(XsltTransformer xsltTransformer) {
		final List<String> attrNameList = node.getAttributeNamesOfNamespace(NAMESPACE_CUSTOM_PARAMETER);
		for (String attrName : attrNameList) {
			//logger.info("attribute: " + attrName);
			final String paramName 	= attrName.replaceAll("(^[^\\{\\}]*:)|(^\\{.*\\})", "");
			final String paramValue	= node.getAttribute(attrName);
			//logger.info("set custom parameter: " + paramName + " = '" + paramValue + "'");
			if (paramValue != null) {
				xsltTransformer.setParameter(new QName(paramName), new XdmAtomicValue(paramValue));
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
