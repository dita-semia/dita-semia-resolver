/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Base;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.HashMap;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.trans.XPathException;

import javax.xml.transform.sax.SAXSource;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;



/**
 * The XslTransformerCache class caches every constructed transformer so they can be reused.
 * XslTransformerCache is a Singleton class.
 * 
 * If no XsltTransformer exists for the specified xsl Source, a new XsltTransformer is constructed.
 * 
 * Every XsltTransformer is connected to the timestamp of the xsl Source. 
 *
 */
public class XslTransformerCache {

	private static final Logger logger = Logger.getLogger(XslTransformerCache.class.getName());
	
	protected final Configuration	configuration;
	
	protected HashMap<URL, ExecutableWithTimestamp> 	executableMap		= new HashMap<>();

	public XslTransformerCache(Configuration configuration) {
		this.configuration 	= configuration;
	}
	
	/**
	 * @return the private XslTransformerCache Instance
	 */
	/*public static XslTransformerCache getInstance() {
		return xslTransformerCache;
	}*/

	/**
	 * Retrieves an Instance of XsltExecutable for the specified URL and URIResolver. 
	 * 
	 * If there is an existing XsltExecutable for this URL and the timestamp of the xsl source matches the timestamp connected to the XsltExecutable, 
	 * this XsltExecutable is returned.
	 * If the URL is invalid, a XPathException is thrown ("Script File could not be found").
	 * If the Script is invalid, a XPathException is thrown ("XSLT compilation error").
	 * 
	 * @param 	url 			URL of the XSL script
	 * @param 	uriResolver		URIResolver of the Node
	 * @return	an existing XsltExecutable if present; a new XsltExecutable, if there is none or if the timestamp is different.
	 * @throws 	XPathException	If the Script File could not be found or if the compilation led to an error.
	 */
	public XsltExecutable getExecutable(URL url, URIResolver uriResolver) throws XPathException {
		
		final String decodedUrl		= FileUtil.decodeUrl(url);
		Timestamp scriptTimestamp 	= new Timestamp(0);
		if (!decodedUrl.startsWith("jar:")) {
			try {
				final URL 	urlDecoded 	= new URL(decodedUrl);
				final File 	script 		= new File(urlDecoded.getFile());
				if (!script.exists()) {
					throw new XPathException("Script file could not be found. (URL: '" + urlDecoded + "')");
				}
				scriptTimestamp = new Timestamp(script.lastModified());
			} catch (MalformedURLException e) {
				throw new XPathException("Script file could not be found. (URL: '" + decodedUrl + "')");
			}
		}
		XsltExecutable xsltExecutable = null;
		if (executableMap.containsKey(url) && executableMap.get(url).getTimestamp().equals(scriptTimestamp)) {
			xsltExecutable = executableMap.get(url).getXsltExecutable(); 
		} else {	
			Source xslSource = null;
			if (uriResolver != null) {
				try {
					xslSource = uriResolver.resolve(url.toExternalForm(), "");
				} catch (TransformerException e) {
					logger.error(e.getMessage());
				}
			}
			if (xslSource == null) {
				xslSource = new SAXSource(new InputSource(url.toExternalForm()));
			} 
			try
			{
				final Processor 	processor 	= new Processor(configuration);
				final XsltCompiler 	compiler 	= processor.newXsltCompiler();
				
				xsltExecutable 	= compiler.compile(xslSource);		
				//logger.info("new Executable: " + xsltExecutable);
				executableMap.put(url,  new ExecutableWithTimestamp(xsltExecutable, scriptTimestamp));
			} 
			catch (SaxonApiException e) {
				throw new XPathException("XSLT compilation error. (URL: '" + url + "'): " + e.getMessage());
			}
		}
		//logger.info("getExecutable: " + xsltExecutable + ", url: " + url);
		return xsltExecutable;
		
	}
	
	/**
	 * Returns an existing XsltExecutable for the specified URL. 
	 * Does not create a new one if there is no existing XsltExecutable.
	 * 
	 * @param url URL of the XSLT script
	 * @return the XsltExecutable for this URL or null if there is none.
	 */
	public XsltExecutable getCachedExecutable(URL url) {
		final ExecutableWithTimestamp exWithTime = executableMap.get(url);
		if (exWithTime != null) {
//			logger.info("getCachedExecutable: " + exWithTime.getXsltExecutable() + ", url: " + url);
			return exWithTime.getXsltExecutable(); 
		} else {
//			logger.info("getCachedExecutable: " + null + ", url: " + url);
			return null;
		}
	}

	/**
	 * Uses {@link XslTransformerCache#getTransformer(URL, URIResolver)} with the specified URL using the URIResolver from the configuration.
	 * @see XslTransformerCache#getTransformer(URL, URIResolver)
	 * 
	 * @param url URL of the XSL script
	 * @return XsltTransformer
	 * @throws XPathException If the Script File could not be found or if the compilation led to an error.
	 */
	public XsltExecutable getExecutable(URL url) throws XPathException {
		return getExecutable(url, configuration.getURIResolver());
	}
	
	/**
	 * Removes the XsltTransformer with the specified URL as key from the Cache.
	 * 
	 * @param url URL of the XSL script
	 */
	public void removeFromCache(URL url)
	{
		executableMap.remove(url);
	}

	private static class ExecutableWithTimestamp {
		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(ExecutableWithTimestamp.class.getName());
		
		private XsltExecutable 	xsltExecutable;
		private Timestamp 		timestamp;
		
		private ExecutableWithTimestamp(XsltExecutable xsltExecutable, Timestamp timestamp) {
			this.xsltExecutable 	= xsltExecutable;
			this.timestamp 	 		= timestamp;
		}
		
		private XsltExecutable getXsltExecutable() {
			return xsltExecutable;
		}
		
		private Timestamp getTimestamp() {
			return timestamp;
		}
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void clear() {
		executableMap.clear();
	}
	
}
