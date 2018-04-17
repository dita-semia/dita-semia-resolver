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
	
	protected HashMap<URL, ExecutableWithTimestamp> 	executableCache		= new HashMap<>();

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
	 * @return	an existing XsltExecutable if present; a new XsltExecutable, if there is none or if the timestamp is different.
	 * @throws 	XPathException	If the Script File could not be found or if the compilation led to an error.
	 */
	public XsltExecutable getExecutable(Source source) throws XPathException {
		URL				url			= null;
		Timestamp 		timestamp 	= null;
		XsltExecutable 	result		= null;
		
		if (source instanceof SAXSource) {
			final SAXSource saxSource = ((SAXSource)source);
			
			// check if cachable
			InputSource inputSource = saxSource.getInputSource();
			if ((inputSource.getCharacterStream() == null) && (inputSource.getByteStream() == null)) {
				// check timestamp
				try {
					url			= new URL(source.getSystemId());
					timestamp 	= new Timestamp(new File(url.getFile()).lastModified());
					ExecutableWithTimestamp cacheEntry = executableCache.get(url);
					if ((cacheEntry != null) && (cacheEntry.getTimestamp().equals(timestamp))) {
						result = cacheEntry.getXsltExecutable();
						//logger.info("from cache: " + url);
					}
					
				} catch (MalformedURLException e) {
					// just ignore it here - will be handled later
				}
			}
		}

		if (result == null) {
			try
			{
				final Processor 	processor 	= new Processor(configuration);
				final XsltCompiler 	compiler 	= processor.newXsltCompiler();
				
				result 	= compiler.compile(source);		
				//logger.info("compiled Executable: " + source.getSystemId());
				
				if (timestamp != null) {
					executableCache.put(url, new ExecutableWithTimestamp(result, timestamp));
					//logger.info("into cache: " + url);
				}
			} 
			catch (SaxonApiException e) {
				throw new XPathException("XSLT compilation error. (URL: '" + source.getSystemId() + "'): " + e.getMessage());
			}
		}

		return result;
	}
	
	
	/**
	 * Returns an existing XsltExecutable for the specified URL. 
	 * Does not create a new one if there is no existing XsltExecutable.
	 * 
	 * @param url URL of the XSLT script
	 * @return the XsltExecutable for this URL or null if there is none.
	 */
	public XsltExecutable getCachedExecutable(URL url) {
		final ExecutableWithTimestamp exWithTime = executableCache.get(url);
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
	 * @param uri URI of the XSL script
	 * @return XsltTransformer
	 * @throws XPathException If the Script File could not be found or if the compilation led to an error.
	 */
	public XsltExecutable getExecutable(String uri) throws XPathException {
		try {
			return getExecutable(configuration.getURIResolver().resolve(uri, ""));
		} catch (TransformerException e) {
			throw new XPathException(e.getMessage(), e);
		}
	}
	
	/**
	 * Removes the XsltTransformer with the specified URL as key from the Cache.
	 * 
	 * @param url URL of the XSL script
	 */
	public void removeFromCache(URL url) {
		executableCache.remove(url);
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void clear() {
		executableCache.clear();
	}

	private static class ExecutableWithTimestamp {
		
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

	public boolean isCompatible(Configuration config) {
		return configuration.isCompatible(config);
	}
	
}
