/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Oxygen;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.xml.transform.URIResolver;

import net.sf.saxon.trans.XPathException;

import org.DitaSemia.Base.NodeWrapper;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorDocument;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.node.AuthorParentNode;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * 
 *
 */
public class AuthorNodeWrapper implements NodeWrapper 
{
	private static final Logger logger = Logger.getLogger(AuthorNodeWrapper.class.getName());

	private AuthorNode 		authorNode;

	private AuthorAccess 	authorAccess;
	
	/**
	 * Constructs an AuthorNodeWrapper with the specified initial AuthorNode and AuthorAccess.
	 * 
	 * @param authorNode 
	 * @param authorAccess 
	 */
	public AuthorNodeWrapper(AuthorNode authorNode, AuthorAccess authorAccess) 
	{
		this.authorNode 	= authorNode;
		this.authorAccess	= authorAccess;
	}

	@Override
	public URL getBaseUri() 
	{
		return authorNode.getXMLBaseURL();
	}

	@Override
	public boolean isElement() 
	{
		return (authorNode != null) && (authorNode.getType() == AuthorNode.NODE_TYPE_ELEMENT);
	}

	@Override
	public String getAttribute(String localName, String namespaceUri) 
	{
		if (isElement()) 
		{
			if (namespaceUri != null) {
				final AttrValue attrValue = ((AuthorElement)authorNode).getAttribute(authorNode.getNamespaceContext().getPrefixForNamespace(namespaceUri) + ":" + localName);
				return (attrValue == null) ? null : attrValue.getValue();
			} else {
				final AttrValue attrValue = ((AuthorElement)authorNode).getAttribute(localName);
				return (attrValue == null) ? null : attrValue.getValue();
			}
		}
		return null;
	}

	@Override
	public NodeWrapper getParent() {
		if (authorNode.getParent() != null) {
			return new AuthorNodeWrapper(authorNode.getParent(), authorAccess);
		} else {
			return null;
		}
	}

	@Override
	public URIResolver getUriResolver() {
		if (authorAccess != null) {
			return authorAccess.getXMLUtilAccess().getURIResolver();
		} else {
			return PluginWorkspaceProvider.getPluginWorkspace().getXMLUtilAccess().getURIResolver();
		}
	}

	@Override
	public String serialize() {
		if (authorAccess != null) {
			try {
				final AuthorDocumentFragment sourceFragment = authorAccess.getDocumentController().createDocumentFragment(authorNode, true);
				return authorAccess.getDocumentController().serializeFragmentToXML(sourceFragment);
			} catch (BadLocationException e) {
				logger.error("Error serializing node (" + authorNode + "): " + e);
			}
		} else {
			logger.error("Can't serialize AuthorNode (" + authorNode + ") without authorAccess.");
		}
		return null;
	}

	@Override
	public int getChildIndexWithinParent() {
		final AuthorNode parent = authorNode.getParent();
		if (parent == null) {
			return -1;
		} else  if (authorNode.getParent() instanceof AuthorDocument) {
			return 1;
		} else {
			//logger.info("parent: " + parent.getDisplayName() + ", type: " + parent.getType() + ", baseUrl: " + parent.getXMLBaseURL());
			return ((AuthorParentNode)parent).getContentNodes().indexOf(authorNode) + 1;
		}
	}
	
	@Override
	public List<String> getAttributeNamesOfNamespace(String namespaceUri) {
		final List<String> list = new LinkedList<String>();
		if (isElement()) {
			final AuthorElement element = ((AuthorElement)authorNode);
			final String		prefix	= authorNode.getNamespaceContext().getPrefixForNamespace(namespaceUri);
			if (prefix != null) {
				//logger.info("attribute count: " + element.getAttributesCount());
				for (int i = 0; i < element.getAttributesCount(); ++i) {
					final String attrName = element.getAttributeAtIndex(i);
					//logger.info("attribute name (" + i + "): " + attrName);
					if (attrName.startsWith(prefix)) {
						list.add(attrName.replaceFirst("^.*:", ""));
					}
				}
			}
		}
		return list;
	}

	@Override
	public String evaluateXPathToString(String xPath) throws XPathException {
//		logger.info("getStringByXPath() AuthorNode");
		if (authorAccess == null) {
			throw new XPathException("AuthorNodeWrapper: Can't evaluate XPath ('" + xPath + "') without AuthorAccess.");
		} else {
			try {
				Object[] Ergebnis = authorAccess.getDocumentController().evaluateXPath(xPath/*Resolved*/, authorNode, false, false, false, false);
				if (Ergebnis.length == 1) {
					return (String)Ergebnis[0];
				} else {
					throw new XPathException("XPath expression ('" + xPath + "') doesn't return a single item.");
				}
			} catch (AuthorOperationException e){
				throw new XPathException("XPath expression ('" + xPath + "') failed to be evaluated.");
			}
		}
	}

}
