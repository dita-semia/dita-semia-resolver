/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Base;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import net.sf.saxon.om.NodeInfo;

/**
 * The FileUtil class is a utility class for handling URLs that should represent Files.
 */
public class FileUtil {

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
				logger.error(e);
				return null;
			}
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
	 * @return true if the File exists, false elsewise.
	 */
	public static boolean fileUrlExists(URL url) {
		if (url != null) {
			final File file = new File(decodeUrl(url).getFile());
			return file.exists();
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
	public static URL decodeUrl(URL url) {
		try {
			return new URL(URLDecoder.decode(url.toExternalForm(), "UTF-8"));
		} catch (Exception e) {
			return null;
		}
	}
}
