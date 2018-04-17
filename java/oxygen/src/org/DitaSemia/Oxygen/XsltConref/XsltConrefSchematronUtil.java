/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Oxygen.XsltConref;

import java.net.URL;

import org.apache.log4j.Logger;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.trans.XPathException;

/**
 * The FileUtil class is a utility class for handling URLs that should represent Files.
 */
public class XsltConrefSchematronUtil {

	private static final Logger logger = Logger.getLogger(XsltConrefSchematronUtil.class.getName());

	
	/**
	 * Checks whether the specified URL represents a well-formed XSL File.
	 * 
	 * @param uri of the XSL File
	 * @return true if the compilation of the File worked, false otherwise.
	 */
	public static boolean isValidXsl(String uri) {
		try {
			return (XsltConrefResolver.getInstance().getTransformerCache().getExecutable(uri) != null);
		} catch (XPathException e) {
			return false;
		} catch (Exception e) {
			logger.error(e, e);
			return false;
		}
	}
	
	/**
	 * Checks whether the specified script expects the specified parameter. 
	 * 
	 * @param scriptUrl URL of the script
	 * @param paramName Name of the parameter
	 * @return true if the parameter is defined, false if it is undefined or if the script is invalid.
	 */
	public static boolean isXslParameterUndefined(URL scriptUrl, String paramName) {
		//logger.info("isXslParameterUndefined(" + scriptUrl + ", " + paramName + ")");
		final XsltExecutable 	xsltExecutable 	= XsltConrefResolver.getInstance().getTransformerCache().getCachedExecutable(scriptUrl);
		final QName 			paramQName 		= new QName(paramName);
		//logger.info(xsltExecutable);
		if ((xsltExecutable != null) && (!xsltExecutable.getGlobalParameters().containsKey(paramQName))) {
			return true;
		} else {
			return false;
		}
	}	
}
