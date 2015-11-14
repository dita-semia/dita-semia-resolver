/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.XsltConref;

import java.io.StringReader;

import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.trans.XPathException;

import org.DitaSemia.JavaBase.AuthorNodeWrapper;
import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
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
import ro.sync.ecss.extensions.dita.conref.DITAConRefResolver;

public class OxygenXsltConrefResolver extends DITAConRefResolver
{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OxygenXsltConrefResolver.class.getName());
	
	@Override
	public String getDescription() 
	{
//		logger.info("getDescription");
		return "Resolves the 'xslt-conref/conkeyref/conref/keyref' references";
	}

	@Override
	public String getDisplayName(AuthorNode node) 
	{
		String displayName = null;
		final XsltConref xsltConref = XsltConref.fromNode(new AuthorNodeWrapper(node, null));
		if (xsltConref != null) {
			displayName = xsltConref.getScriptUrl().toString();
		} else {
			displayName = super.getDisplayName(node);
		}
//		logger.info("getDisplayName: " + displayName);
		return displayName;
	}

	@Override
	public String getReferenceSystemID(AuthorNode node, AuthorAccess authorAccess) 
	{
		String systemID = null;
		if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null))) {
			systemID = node.getXMLBaseURL().toExternalForm();
		} else {
			systemID = super.getReferenceSystemID(node, authorAccess);
		}
		//logger.info("getReferenceSystemID: " + systemID);
		return systemID;
	}

	@Override
	public String getReferenceUniqueID(AuthorNode node) 
	{
		String referenceUniqueID = null;
		final XsltConref xsltConref = XsltConref.fromNode(new AuthorNodeWrapper(node, null));
		if (xsltConref != null) {
			referenceUniqueID = xsltConref.getScriptUrlAsString().toString();
		} else {
			referenceUniqueID = super.getReferenceUniqueID(node);
		}
		//logger.info("getReferenceUniqueID: " + referenceUniqueID);
		return referenceUniqueID;	
	}

	@Override
	public boolean hasReferences(AuthorNode node) 
	{
		boolean hasReferences = false;
		if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null))) {
			hasReferences = true;
		} else {
			hasReferences = super.hasReferences(node);
		}
		//logger.info("hasReferences: " + hasReferences);
		return hasReferences;
	}

	@Override
	public boolean isReferenceChanged(AuthorNode node, String attributeName) 
	{
		//logger.info("isReferenceChanged");
		boolean isChanged = false;
		if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null))) {
			return isXsltConrefAttr(node, attributeName);
		} else {
			isChanged = super.isReferenceChanged(node, attributeName);
		}
		return isChanged;
	}
	
	public static boolean isXsltConrefAttr(AuthorNode node, String attributeName) {

		final String customParameterPrefix = node.getNamespaceContext().getPrefixForNamespace(XsltConref.NAMESPACE_CUSTOM_PARAMETER);
		return (attributeName.equals(XsltConref.ATTR_URI)) ||
				(attributeName.equals(XsltConref.ATTR_XML_SOURCE_URI)) ||
				(attributeName.equals(XsltConref.ATTR_START_TEMPLATE)) ||
				(attributeName.startsWith(customParameterPrefix + ":"));
	}

	@Override
	public void checkTarget(AuthorNode node, AuthorDocument targetDocument) throws ValidatingReferenceResolverException
	{
		if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null))) {
			checkXsltConrefTarget(node, targetDocument);	
		} else {
			super.checkTarget(node, targetDocument);
		}
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
	
	@Override
	public SAXSource resolveReference(AuthorNode node, String systemID, AuthorAccess authorAccess, EntityResolver entityResolver) throws ReferenceResolverException
	{
		final XsltConref 	xsltConref 	= XsltConref.fromNode(new AuthorNodeWrapper(node, authorAccess));
		SAXSource 			saxSource 	= null;
		if (xsltConref != null) {
			saxSource = resolveXsltConref(xsltConref, authorAccess);
		} else {
			saxSource = super.resolveReference(node, systemID, authorAccess, entityResolver);
		}
		return saxSource;
	}

	public static SAXSource resolveXsltConref(XsltConref xsltConref, AuthorAccess authorAccess) throws ReferenceResolverException
	{
		String resolvedString;
		try {
			resolvedString = xsltConref.resolve().serialize();
		} catch (XPathException e) {
			throw new ReferenceResolverException(e.getMessage(), true, true);
		}
		final XMLReader xmlReader = authorAccess.getXMLUtilAccess().newNonValidatingXMLReader();
		final SAXSource	saxSource = new SAXSource(xmlReader, new InputSource(new StringReader(resolvedString)));
		//logger.info("resolvedString: " +  resolvedString);
		
		return saxSource;
	}
}
