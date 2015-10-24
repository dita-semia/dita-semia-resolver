/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.XsltConref;

import java.net.MalformedURLException;
import java.net.URL;

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
	
	public static final String 			ATTR_URL 			= "xslt-conref";
	private final NodeWrapper 			node;
	private final XslTransformerCache 	transformerCache 	= XslTransformerCache.getTransformerCache();
	
	public static XsltConref fromNode(NodeWrapper node)
	{
		if (isXsltConref(node)) 
		{
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
	
	public NodeWrapper resolve()
	{
		final URL 			scriptUrl 	= node.resolveUrl(getScriptUriString());
		final SAXSource 	xmlSource 	= new SAXSource(new InputSource(node.getBaseUri().toExternalForm()));
		DOMResult 			result 		= new DOMResult();
		final Transformer 	transformer = transformerCache.getTransformer(scriptUrl);
		
		//logger.info("scriptUrl: " + scriptUrl);
		
		try 
		{
			transformer.transform(xmlSource, result);
		} 
		catch (TransformerException te) 
		{
			te.printStackTrace();
			logger.error("Exception while transforming: " + te.getMessage());
		}

		return new DomNodeWrapper(result.getNode());
	}

	public URL getScriptUri() 
	{
		URL url = null;
		try 
		{
			url = new URL(getScriptUriString());
		} 
		catch (MalformedURLException e) 
		{
			logger.error("MalformedURLException: " + e.getMessage());
		}
//		logger.info("getScriptUri: " + url);
		return url;
	}
	
	public String getScriptUriString()
	{
		return node.getAttribute(ATTR_URL);
	}
}
