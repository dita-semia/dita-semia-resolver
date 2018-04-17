/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;

import javax.xml.transform.TransformerException;
import org.apache.log4j.Logger;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;

/**
 * The FileUtil class is a utility class for handling URLs that should represent Files.
 */
public class FileUtil {
	
	public static final String 				TIMESTAMP_FORMAT_PATTERN 	= "yyyy-MM-dd HH:mm:ss.SSS";
	public static final SimpleDateFormat	TIMESTAMP_FORMAT			= new SimpleDateFormat(TIMESTAMP_FORMAT_PATTERN);
	

	public static final String TEMP_DIR_MARKER = "t1/t2/t3/t4/t5";

	private static final Logger logger = Logger.getLogger(FileUtil.class.getName());

	/**
	 * Resolves the specified String to an URL, relative to the specified nodes Base URL.
	 * 
	 * @param uri String representing the URL
	 * @param node context node
	 * @return null if the String does not represent a well-formed URL 
	 */
	// URI / URL?
	public static URL resolveUri(String uri, NodeInfo node) {
		if ((uri != null) && (!uri.isEmpty())) {
			try {
				return new URL(node.getConfiguration().getURIResolver().resolve(uri, node.getBaseURI()).getSystemId());
			} catch (MalformedURLException | TransformerException e) {
				logger.error(e, e);
			}
			return null;
		} else {
			return null;
		}
	}
	
	/**
	 * Checks whether the specified String represents an URL that belongs to an existing File.
	 * Returns 	true if the specified String represents an URL that belongs to an existing File, 
	 * 			false if the specified String does not represent an URL or the URL does not belong 
	 * 			to an existing File.
	 * 
	 * @param urlString
	 * @return true if the file exists, false if the file does not exist or if the URL is not well-formed
	 */
	public static boolean fileExists(String urlString) {
		try {
			return fileUrlExists(new URL(urlString));
		} catch (MalformedURLException e) {
			return false;
		}
	}
	
	/**
	 * Checks whether the specified URL represents an existing File.
	 * 
	 * @param url 
	 * @return true if the File exists, false otherwise.
	 */
	public static boolean fileUrlExists(URL url) {
		if (url != null) {
			try {
				final URL 	decodedUrl 	= new URL(decodeUrl(url));
				final File 	file 		= new File(decodedUrl.getFile());
				return file.exists();
			} catch (MalformedURLException e) {
				return false;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Decodes the specified URL using the {@link java.net.URLDecoder}.
	 * 
	 * The URL is decoded with UTF-8.
	 * 
	 * @param url
	 * @return the decoded URL or null if an error occurred.
	 */
	public static String decodeUrl(URL url) {
		if (url != null) {
			return decodeUrl(url.toExternalForm());
		} else {
			return null;
		}
	}
	
	/**
	 * Decodes the specified URL using the {@link java.net.URLDecoder}.
	 * 
	 * The URL is decoded with UTF-8.
	 * 
	 * @param url
	 * @return the decoded URL or null if an error occurred.
	 */
	public static String decodeUrl(String url) {
		if (url != null) {
			try {
				return URLDecoder.decode(url, "UTF-8");
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static long getLastModified(String systemId) {
		try {
			final URL 	url 		= new URL(systemId);
			final File	file		= new File(decodeUrl(url.getFile())); 
			return file.lastModified();
		} catch (MalformedURLException e) {
			return 0;
		}
	}
	
	public static String getLastModifiedAsString(String systemId) {
		final long 	timestamp 	= getLastModified(systemId);
		if (timestamp > 0) {
			return TIMESTAMP_FORMAT.format(timestamp);
		} else {
			return null;
		}
	}
	
	public static String readFileToString(File file) throws XPathException {

	    try (BufferedReader reader = new BufferedReader(new FileReader(file));) {
		    String         line 			= null;
		    StringBuilder  stringBuilder 	= new StringBuilder();
		    String         ls 				= System.getProperty("line.separator");
	        while((line = reader.readLine()) != null) {
	            stringBuilder.append(line);
	            stringBuilder.append(ls);
	        }

	        return stringBuilder.toString();
	    } catch (IOException e) {
			logger.error(e, e);
	    	throw new XPathException("Unable to read file: '" + file.getAbsolutePath() + "'. (" + e.getMessage() + ")");
		} 
	}



	public static URL getFixedBaseUrl(NodeWrapper node, String baseDir) {
		
		// set original base-url of file when processed by DITA-OT
		final String xtrfString = node.getAttribute("xtrf", null);
		if (xtrfString != null) {
			String url;
			if (xtrfString.contains(TEMP_DIR_MARKER)) {
				// handle fix.external.refs.com.oxygenxml
				url = "file:/" + baseDir + xtrfString.substring(xtrfString.indexOf(TEMP_DIR_MARKER) + TEMP_DIR_MARKER.length());
			} else {
				url = xtrfString;
			}
			try {
				return new URL(url);
			} catch (MalformedURLException e) {
				logger.error(e, e);
				return null;
			}
		} else {
			return node.getBaseUrl();
		}
	}
}
