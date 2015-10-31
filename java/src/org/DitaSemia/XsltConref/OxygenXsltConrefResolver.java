/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.XsltConref;

import java.io.StringReader;
import java.net.URL;

import javax.xml.transform.sax.SAXSource;

import org.DitaSemia.JavaBase.AuthorNodeWrapper;
import org.DitaSemia.JavaBase.NodeWrapper;
import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import ro.sync.ecss.extensions.api.AuthorAccess;
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
		if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null)))
		{
			AuthorElement 	element 	= (AuthorElement) node;
			AttrValue 		attrValue 	= element.getAttribute("class");
			AttrValue 		attrValue2 	= element.getAttribute("xslt-conref");
			
			StringBuilder 	sb 			= new StringBuilder();
			sb.append(attrValue.getValue());
			sb.append(attrValue2.getValue());
			
			displayName = sb.toString();
		}
		else
		{
			displayName = super.getDisplayName(node);
		}
//		logger.info("getDisplayName: " + displayName);
		return displayName;
	}

	@Override
	public String getReferenceSystemID(AuthorNode node, AuthorAccess authorAccess) 
	{
		String systemID = null;
		if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null)))
		{
			NodeWrapper 		nodeWrapper	= new AuthorNodeWrapper(node, authorAccess);
			final XsltConref 	xsltConref 	= XsltConref.fromNode(nodeWrapper);
			final URL 			url 		= xsltConref.getScriptUrl();
			systemID 						= url.toString();
		}
		else
		{
			systemID = super.getReferenceSystemID(node, authorAccess);
		}
		//logger.info("getReferenceSystemID: " + systemID);
		return systemID;
	}

	@Override
	public String getReferenceUniqueID(AuthorNode node) 
	{
		String referenceUniqueID = null;
		if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null)))
		{
			AuthorElement 	element 	= (AuthorElement) node;
			AttrValue 		attrValue 	= element.getAttribute("xslt-conref");
			referenceUniqueID 			= attrValue.getValue();
		}
		else
		{
			referenceUniqueID = super.getReferenceUniqueID(node);
		}
		//logger.info("getReferenceUniqueID: " + referenceUniqueID);
		return referenceUniqueID;	
	}

	@Override
	public boolean hasReferences(AuthorNode node) 
	{
		boolean hasReferences = false;
		if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null)))
		{
			hasReferences = true;
		}
		else
		{
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
			final String customParameterPrefix = node.getNamespaceContext().getPrefixForNamespace(XsltConref.NAMESPACE_CUSTOM_PARAMETER);
			isChanged = 
					(attributeName.equals(XsltConref.ATTR_URL)) ||
					(attributeName.equals(XsltConref.ATTR_XML_SOURCE_URL)) ||
					(attributeName.startsWith(customParameterPrefix + ":"));
		} else {
			isChanged = super.isReferenceChanged(node, attributeName);
		}
		return isChanged;
	}

	@Override
	public void checkTarget(AuthorNode node, AuthorDocument targetDocument) throws ValidatingReferenceResolverException
	{
		if ((XsltConref.isXsltConref(new AuthorNodeWrapper(node, null))) &&
			(targetDocument.getRootElement().getDisplayName().equals(XsltConref.NAME_NO_CONTENT))) {
			// no-content is always valid as result element
		} else {
			super.checkTarget(node, targetDocument);
		}
	}
	
	public SAXSource resolveReference(AuthorNode node, String systemID, AuthorAccess authorAccess, EntityResolver entityResolver) 
	{	
		//logger.info("resolveReference");
		//logger.info("node URL: " + node.getXMLBaseURL().toExternalForm());
		final NodeWrapper 	nodeWrapper = new AuthorNodeWrapper(node, authorAccess);
		final XsltConref 	xsltConref 	= XsltConref.fromNode(nodeWrapper);
		SAXSource 			saxSource 	= null;
		
		if (xsltConref != null)
		{
			final String 	resolvedString 	= xsltConref.resolve().serialize();
			final XMLReader xmlReader 		= authorAccess.getXMLUtilAccess().newNonValidatingXMLReader();
			saxSource = new SAXSource(xmlReader, new InputSource(new StringReader(resolvedString)));
			//logger.info("resolvedString: " +  resolvedString);
		}
		else
		{
			saxSource = super.resolveReference(node, systemID, authorAccess, entityResolver);
		}
		
		return saxSource;
	}
}
