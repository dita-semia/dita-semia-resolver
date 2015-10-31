/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.XsltConref;

import java.io.StringReader;
import java.net.URL;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;






import org.DitaSemia.JavaBase.DomNodeWrapper;
import org.DitaSemia.JavaBase.NodeWrapper;
import org.DitaSemia.JavaBase.XslTransformerCache;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

public class XsltConref 
{
//	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(XsltConref.class.getName());
	
	public static final String 	ATTR_URL 					= "xslt-conref";
	public static final String 	ATTR_XML_SOURCE_URL			= "xslt-conref-source";
	public static final String 	PARAM_XPATH_TO_XSLT_CONREF	= "xPathToXsltConref";
	public static final String 	NAME_NO_CONTENT				= "no-content";
	public static final String 	NAMESPACE_CUSTOM_PARAMETER	= "http://www.dita-semia.org/xslt-conref/custom-parameter";
	
	
	private final NodeWrapper 			node;
	
	public static XsltConref fromNode(NodeWrapper node)
	{
		if (isXsltConref(node)) {
			return new XsltConref(node);
		}
		return null;
	}
	
	private XsltConref(NodeWrapper node)
	{
		this.node = node;
	}
	
	public static boolean isXsltConref(NodeWrapper node)
	{
		boolean isXsltConref = ((node != null) && 
								(node.isElement()) && 
								(node.getAttribute(XsltConref.ATTR_URL) != null) &&
								(!node.getAttribute(XsltConref.ATTR_URL).isEmpty()));
		return isXsltConref;
	}
	
	public NodeWrapper resolve() {
		try {
			final URL 			scriptUrl 	= getScriptUrl();
			final Transformer 	transformer = XslTransformerCache.getInstance().getTransformer(scriptUrl);
			SAXSource 			xmlSource 	= null;
			DOMResult 			result 		= new DOMResult();
			
			if (getStartTemplate() != null) {
				// TODO: create empty source
				// TODO: set start template
			} else if (getXmlSourceUrl() != null) {
				xmlSource = new SAXSource(new InputSource(getXmlSourceUrl().toExternalForm()));
			} else {
				// use current document as input
				
				// Create string from parsed document instead of using baseUri so document doesn't have to be saved.
				xmlSource = createSaxSourceFromCurrentDocument();

				// set specific standard parameter
				transformer.setParameter(PARAM_XPATH_TO_XSLT_CONREF, createXPathToElement(node));
			}

			setCustomParamters(transformer);

			//logger.info("scriptUrl: " + scriptUrl);
			transformer.transform(xmlSource, result);

			return new DomNodeWrapper(result.getNode());

		} catch (TransformerException e) {
			logger.error("Exception while transforming: " + e.getMessage(), e);
			return null;
			// TODO: throw exception with error message to be displayed properly (not only in log file!)
		} catch (Exception e) {
			logger.error(e, e);
			return null;
			// 	TODO: throw exception with error message to be displayed properly (not only in log file!)
		}
	}

	
	public URL getScriptUrl() {
		return node.resolveUrl(node.getAttribute(ATTR_URL));
	}

	
	public URL getXmlSourceUrl() {
		final String attrValue = node.getAttribute(ATTR_XML_SOURCE_URL);
		if ((attrValue != null) && (!attrValue.isEmpty())) {
			return node.resolveUrl(attrValue);
		} else {
			return null;
		}
	}

	public Object getStartTemplate() {
		// TODO: ...
		return null;
	}
	
	private SAXSource createSaxSourceFromCurrentDocument() {
		final String 	serializedSource 	= node.getDocument().serialize();
		final SAXSource saxSource 			= new SAXSource(new InputSource(new StringReader(serializedSource)));

		//logger.info("Serialized source: " + serializedSource);
		
		saxSource.setSystemId(node.getBaseUri().toExternalForm());
		
		return saxSource;
	}

	
	private void setCustomParamters(Transformer transformer) {
		final List<String> attrNameList = node.getAttributeNamesOfNamespace(NAMESPACE_CUSTOM_PARAMETER);
		for (String attrName : attrNameList) {
			//logger.info("attribute: " + attrName);
			final String paramName 	= attrName.replaceAll("(^[^\\{\\}]*:)|(^\\{.*\\})", "");
			final String paramValue	= node.getAttribute(attrName);
			//logger.info("set custom parameter: " + paramName + " = '" + paramValue + "'");
			if (paramValue != null) {
				transformer.setParameter(paramName, paramValue);
			}
		}
	}
	
	
	private static String createXPathToElement(NodeWrapper node) {
		String createXPathToElement = "";
		
		while ((node != null) && (node.getParent() != null)) {
			createXPathToElement = "/*[" + node.getChildIndexWithinParent() + "]" + createXPathToElement;
			node = node.getParent();
		}
		
		//logger.info("createXPathToElement: result = " + createXPathToElement);
		return createXPathToElement;
	}
}
