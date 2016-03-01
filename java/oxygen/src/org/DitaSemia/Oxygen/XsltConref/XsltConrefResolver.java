/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Oxygen.XsltConref;

import java.io.StringReader;

import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.jaxp.TransformerImpl;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;

import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.XPathCache;
import org.DitaSemia.Base.XslTransformerCache;
import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import ro.sync.ecss.dita.DITAAccess;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.ReferenceResolverException;
import ro.sync.ecss.extensions.api.ValidatingReferenceResolverException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorDocument;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.util.XMLUtilAccess;

public class XsltConrefResolver {
	
	private static final Logger logger = Logger.getLogger(XsltConrefResolver.class.getName());
	

	protected static XslTransformerCache	transformerCache	= null;
	protected static XPathCache				xPathCache			= null;
	protected static boolean				staticInitComplete	= false;

	protected static void initStatic() {
		if (!staticInitComplete) {

			staticInitComplete 	= true;
			
			try {
				final Configuration configuration = XsltConref.createConfiguration();
				
				/*
				 * To allow the error and output messages to be displayed within oxygen and to use the catalogs the configuration needs to contain the handlers.
				 * To get these create a transformer through oXygen API can take the required handlers from its configuration. 
				 */
				final String 			dummyXsl 	= "<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\"/>";
				final TransformerImpl 	temp 		= (TransformerImpl)PluginWorkspaceProvider.getPluginWorkspace().getXMLUtilAccess().createXSLTTransformer(
						new SAXSource(new org.xml.sax.InputSource(new StringReader(dummyXsl))), 
						null, 
						XMLUtilAccess.TRANSFORMER_SAXON_HOME_EDITION,
						false);
				Configuration baseConfiguration = temp.getConfiguration();
				
				configuration.setErrorListener(baseConfiguration.getErrorListener());
				configuration.setLogger(baseConfiguration.getLogger());
				configuration.setModuleURIResolver(baseConfiguration.getModuleURIResolver());
				configuration.setOutputURIResolver(baseConfiguration.getOutputURIResolver());
				configuration.setStandardErrorOutput(baseConfiguration.getStandardErrorOutput());
				configuration.setTraceListener(baseConfiguration.getTraceListener());
				configuration.setURIResolver(baseConfiguration.getURIResolver());
				configuration.setSourceResolver(baseConfiguration.getSourceResolver());
				configuration.setSourceParserClass(baseConfiguration.getSourceParserClass());

				configuration.getDefaultXsltCompilerInfo().setMessageReceiverClassName(baseConfiguration.getDefaultXsltCompilerInfo().getMessageReceiverClassName());
				configuration.getDefaultXsltCompilerInfo().setOutputURIResolver(baseConfiguration.getDefaultXsltCompilerInfo().getOutputURIResolver());
				configuration.getDefaultXsltCompilerInfo().setURIResolver(baseConfiguration.getDefaultXsltCompilerInfo().getURIResolver());

				transformerCache	= new XslTransformerCache(configuration);
				xPathCache			= new XPathCache(configuration);
				
			} catch (Exception e) {
				logger.error(e, e);
			}
		}
	}
	
	public static XsltConref xsltConrefFromNode(AuthorNode node, AuthorAccess authorAccess) {
		initStatic();
		final XsltConref xsltConref = XsltConref.fromNode(new AuthorNodeWrapper(node, authorAccess), transformerCache, xPathCache);
		return xsltConref;
	}

	public static boolean isXsltConrefAttr(AuthorNode node, String attributeName) {
		final String customParameterPrefix = node.getNamespaceContext().getPrefixForNamespace(XsltConref.NAMESPACE_PARAMETER_URI);
		return (attributeName.equals(XsltConref.ATTR_URI)) ||
				(attributeName.equals(XsltConref.ATTR_XML_SOURCE_URI)) ||
				(attributeName.equals(XsltConref.ATTR_START_TEMPLATE)) ||
				(attributeName.startsWith(customParameterPrefix + ":"));
	}

	
	public static void checkXsltConrefTarget(AuthorNode node, AuthorDocument targetDocument) throws ValidatingReferenceResolverException {
		
		if (targetDocument.getRootElement().getDisplayName().equals(XsltConref.NAME_NO_CONTENT)) {
			// <no-content> is always valid as result element
			return;
		}
		
		String errorMessage = null;
	    
	    final AuthorElement targetElement = targetDocument.getRootElement();
	    if (targetElement != null) {
	        final AttrValue targetClass = targetElement.getAttribute("class");
	        if (targetClass == null) {
	        	errorMessage = "The target does not have a class attribute";
	        } else {
	        	if (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
	        		final AttrValue sourceClass = ((AuthorElement)node).getAttribute("class");
	        		if (sourceClass != null) {
	                    final boolean isGeneralization = DITAAccess.isGeneralizationOf(targetClass.getValue(), sourceClass.getValue());
	                    if (!isGeneralization) {
	                    	errorMessage = 
                    			"The target element '" + targetElement.getName() +"' " +
                    			"with class value '" + targetClass.getValue() +"' " +
                    			"is not a generalization of source element '" + node.getName() +"' " + 
                    			"with class value '" + sourceClass.getValue() + "'";
	                    }
	        		} else {
	        			errorMessage = "The source does not have a class attribute";
	        		}
	        	} else {
	        		errorMessage = "The XSLT-Conref source is not an element";
	        	}
	        }
	    }
	    if (errorMessage != null) {
	      throw new ValidatingReferenceResolverException("XSLT-Conref was not expanded:\n" + errorMessage);
	    }
    }
	
	public static SAXSource resolveXsltConref(XsltConref xsltConref, AuthorAccess authorAccess) throws ReferenceResolverException {
		String resolvedString;
		try {
			final NodeInfo resolvedNode = xsltConref.resolve();
			resolvedString = SaxonNodeWrapper.serializeNode(resolvedNode);
		} catch (XPathException e) {
			throw new ReferenceResolverException(e.getMessage(), true, true);
		}
		final XMLReader xmlReader = authorAccess.getXMLUtilAccess().newNonValidatingXMLReader();
		final SAXSource	saxSource = new SAXSource(xmlReader, new InputSource(new StringReader(resolvedString)));
		//logger.info("resolvedString: " +  resolvedString);
		
		return saxSource;
	}

	public static XslTransformerCache getTransformerCache() {
		initStatic();
		return transformerCache;
	}
}
