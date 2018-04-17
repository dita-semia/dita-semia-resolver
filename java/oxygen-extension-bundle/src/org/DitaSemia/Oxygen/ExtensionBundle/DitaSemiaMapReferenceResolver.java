/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Oxygen.ExtensionBundle;

import javax.xml.transform.sax.SAXSource;

import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.XsltConref.XsltConrefResolver;
import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.ValidatingReferenceResolverException;
import ro.sync.ecss.extensions.api.node.AuthorDocument;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.dita.map.topicref.DITAMapRefResolver;

public class DitaSemiaMapReferenceResolver extends DITAMapRefResolver {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DitaSemiaMapReferenceResolver.class.getName());

	private boolean active = true;
	
	@Override
	public String getDescription() {
		//logger.info("getDescription");
		return "Resolves the 'xslt-conref/conkeyref/conref/keyref' references";
	}

	@Override
	public String getDisplayName(AuthorNode node) {
		if (active) {
			String displayName = null;
			final XsltConref xsltConref = XsltConrefResolver.getInstance().xsltConrefFromNode(node, null, false);
			if (xsltConref != null) {
				displayName = xsltConref.getUniqueId();
			} else {
				displayName = super.getDisplayName(node);
			}
			//logger.info("getDisplayName: " + displayName);
			return displayName;
		} else {
			return "";
		}
	}

	@Override
	public String getReferenceSystemID(AuthorNode node, AuthorAccess authorAccess) {
		if (active) {
			String systemID = null;
			if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null), false)) {
				systemID = node.getXMLBaseURL().toExternalForm();
			} else {
				systemID = super.getReferenceSystemID(node, authorAccess);
			}
			//logger.info("getReferenceSystemID: " + systemID);
			return systemID;
		} else {
			return "";
		}
	}

	@Override
	public String getReferenceUniqueID(AuthorNode node) {
		if (active) {
			String referenceUniqueID = null;
			final XsltConref xsltConref = XsltConrefResolver.getInstance().xsltConrefFromNode(node, null, false);
			if (xsltConref != null) {
				referenceUniqueID = xsltConref.getUniqueId();
			} 
			//logger.info("getReferenceUniqueID: " + referenceUniqueID);
			return (referenceUniqueID == null) ? super.getReferenceUniqueID(node) : referenceUniqueID;
		} else {
			return "";
		}
	}

	@Override
	public boolean hasReferences(AuthorNode node) {
		if (active) {
			boolean hasReferences = false;
			if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null), false)) {
				hasReferences = true;
			} else {
				hasReferences = super.hasReferences(node);
			}
			//logger.info("hasReferences: " + hasReferences);
			return hasReferences;
		} else {
			return false;
		}
	}

	@Override
	public boolean isReferenceChanged(AuthorNode node, String attributeName) {
		if (active) {
			//logger.info("isReferenceChanged");
			boolean isChanged = false;
			if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null), false)) {
				return XsltConrefResolver.isXsltConrefAttr(node, attributeName);
			} else {
				isChanged = super.isReferenceChanged(node, attributeName);
			}
			return isChanged;
		} else {
			return false;
		}
	}

	@Override
	public void checkTarget(AuthorNode node, AuthorDocument targetDocument) throws ValidatingReferenceResolverException {
		if (active) {
			if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null), false)) {
				XsltConrefResolver.checkXsltConrefTarget(node, targetDocument);
			} else {
				super.checkTarget(node, targetDocument);
			}
		}
	}

	public SAXSource resolveReference(AuthorNode node, String systemID, AuthorAccess authorAccess, EntityResolver entityResolver) {
		if (active) {
			final XsltConref	xsltConref 	= XsltConrefResolver.getInstance().xsltConrefFromNode(node, authorAccess, false);
			SAXSource 			saxSource 	= null;
			if (xsltConref != null) {
				saxSource = XsltConrefResolver.getInstance().resolveXsltConref(xsltConref, authorAccess);
			} else { 
				saxSource = super.resolveReference(node, systemID, authorAccess, entityResolver);
			}
			//logger.info("saxSource: " + saxSource);
			return saxSource;
		} else {
			return null;
		}
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return active;
	}
}
