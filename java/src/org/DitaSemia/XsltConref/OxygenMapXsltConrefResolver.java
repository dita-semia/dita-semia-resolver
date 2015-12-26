/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.XsltConref;

import java.net.URL;

import javax.xml.transform.sax.SAXSource;

import org.DitaSemia.JavaBase.AuthorNodeWrapper;
import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.ValidatingReferenceResolverException;
import ro.sync.ecss.extensions.api.node.AuthorDocument;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.dita.map.topicref.DITAMapRefResolver;

public class OxygenMapXsltConrefResolver extends DITAMapRefResolver
{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OxygenMapXsltConrefResolver.class.getName());
	
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
			displayName = xsltConref.getScriptUrlAsString();
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
			URL scriptUrl = xsltConref.getScriptUrl();
			if (scriptUrl != null) {
				referenceUniqueID = xsltConref.getScriptUrl().toString();
			} else {
				referenceUniqueID = super.getReferenceUniqueID(node);
			}
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
			return OxygenXsltConrefResolver.isXsltConrefAttr(node, attributeName);
		} else {
			isChanged = super.isReferenceChanged(node, attributeName);
		}
		return isChanged;
	}

	@Override
	public void checkTarget(AuthorNode node, AuthorDocument targetDocument) throws ValidatingReferenceResolverException
	{
		if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null))) {
			OxygenXsltConrefResolver.checkXsltConrefTarget(node, targetDocument);
		} else {
			super.checkTarget(node, targetDocument);
		}
	}

	public SAXSource resolveReference(AuthorNode node, String systemID, AuthorAccess authorAccess, EntityResolver entityResolver) 
	{
		final XsltConref 	xsltConref 	= XsltConref.fromNode(new AuthorNodeWrapper(node, authorAccess));
		SAXSource 			saxSource 	= null;
		if (xsltConref != null) {
			saxSource = OxygenXsltConrefResolver.resolveXsltConref(xsltConref, authorAccess);
		} else { 
			saxSource = super.resolveReference(node, systemID, authorAccess, entityResolver);
		}
		//logger.info("saxSource: " + saxSource);
		return saxSource;
	}
}
