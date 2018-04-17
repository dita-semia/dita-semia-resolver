/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Oxygen.ExtensionBundle;

import javax.xml.transform.sax.SAXSource;

import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.DynamicXmlDefinition.OxygenDxdCodeblockResolver;
import org.DitaSemia.Oxygen.XsltConref.XsltConrefResolver;
import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.ReferenceResolverException;
import ro.sync.ecss.extensions.api.ValidatingReferenceResolverException;
import ro.sync.ecss.extensions.api.node.AuthorDocument;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.dita.conref.DITAConRefResolver;

public class DitaSemiaReferenceResolver extends DITAConRefResolver {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DitaSemiaReferenceResolver.class.getName());

	private boolean active = true;
	
	protected final XsltConrefResolver 			xsltConrefResolver;
	protected final OxygenDxdCodeblockResolver 	dxdCodeblockResolver;

	public DitaSemiaReferenceResolver() {
		xsltConrefResolver 		= XsltConrefResolver.getInstance();
		dxdCodeblockResolver	= OxygenDxdCodeblockResolver.getInstance();
	}

	protected DitaSemiaReferenceResolver(XsltConrefResolver xsltConrefResolver) {
		this.xsltConrefResolver = xsltConrefResolver;
		dxdCodeblockResolver	= OxygenDxdCodeblockResolver.getInstance();
	}

	@Override
	public String getDescription()  {
		return "Resolves the 'xslt-conref/conkeyref/conref/keyref' references";
	}

	@Override
	public String getDisplayName(AuthorNode node) {
		if (active) {
			String displayName = null;
			final XsltConref 	xsltConref 	= xsltConrefResolver.xsltConrefFromNode(node, null, false);
			if (xsltConref != null) {
				displayName = xsltConref.getUniqueId();
			} else if (OxygenDxdCodeblockResolver.isDxdCodeblock(node)) {
				displayName = dxdCodeblockResolver.getDisplayName(node);
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
			} else if (OxygenDxdCodeblockResolver.isDxdCodeblock(node)) {
				systemID = dxdCodeblockResolver.getReferenceSystemId(node);
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
			final XsltConref xsltConref = xsltConrefResolver.xsltConrefFromNode(node, null, false);
			if (xsltConref != null) {
				if (!xsltConref.isCopy()) {
					referenceUniqueID = xsltConref.getUniqueId();
				}
			} else if (OxygenDxdCodeblockResolver.isDxdCodeblock(node)) {
				referenceUniqueID = dxdCodeblockResolver.getUniqueId(node);
			} else {
				referenceUniqueID = super.getReferenceUniqueID(node);
			}
			//logger.info("getReferenceUniqueID: " + referenceUniqueID);
			return referenceUniqueID;
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
			} else if (OxygenDxdCodeblockResolver.isDxdCodeblock(node)) {
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
			boolean isChanged = false;
			if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null), false)) {
				return XsltConrefResolver.isXsltConrefAttr(node, attributeName);
			} else if (OxygenDxdCodeblockResolver.isDxdCodeblock(node)) {
				return dxdCodeblockResolver.isDxdCodeblockAttr(node, attributeName);
			} else {
				isChanged = super.isReferenceChanged(node, attributeName);
			}
			//logger.info("isReferenceChanged(" + attributeName + "): " + isChanged);
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
			} else if (OxygenDxdCodeblockResolver.isDxdCodeblock(node)) {
				dxdCodeblockResolver.checkTarget(node, targetDocument);
			} else {
				super.checkTarget(node, targetDocument);
			}
		}
	}
	
	@Override
	public SAXSource resolveReference(AuthorNode node, String systemID, AuthorAccess authorAccess, EntityResolver entityResolver) throws ReferenceResolverException {
		if (active) {
			final XsltConref 	xsltConref 	= xsltConrefResolver.xsltConrefFromNode(node, authorAccess, false);
			SAXSource 			saxSource 	= null;
			if ((xsltConref != null) && (!xsltConref.isCopy())) {
				saxSource = xsltConrefResolver.resolveXsltConref(xsltConref, authorAccess);
			} else if (OxygenDxdCodeblockResolver.isDxdCodeblock(node)) {
				saxSource = dxdCodeblockResolver.resolve(node, systemID, authorAccess, entityResolver);
			} else {
				saxSource = super.resolveReference(node, systemID, authorAccess, entityResolver);
			}
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
