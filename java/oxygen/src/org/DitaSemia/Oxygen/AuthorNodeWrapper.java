/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Oxygen;

import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.xml.transform.URIResolver;

import net.sf.saxon.trans.XPathException;

import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.XPathNotAvaliableException;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.Content;
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
	public URL getBaseUrl() 
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
				final 		String		namespacePrefix	= authorNode.getNamespaceContext().getPrefixForNamespace(namespaceUri);
				/*final*/ 	AttrValue 	attrValue 		= ((AuthorElement)authorNode).getAttribute(namespacePrefix + ":" + localName);
				
				// <TEMP>
				// oXygen doesn't handle default attribues within a namesapce from XSD correctly.
				// Workaround: check for attribute without namespace as well.
				if (attrValue == null) {
					attrValue = ((AuthorElement)authorNode).getAttribute(localName);	
				}
				// </TEMP>
				
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
	public int getChildElementIndexWithinParent() {
		final AuthorNode parent = authorNode.getParent();
		if (parent == null) {
			return -1;
		} else  if (authorNode.getParent() instanceof AuthorDocument) {
			return 1;
		} else {
			//logger.info("parent: " + parent.getDisplayName() + ", type: " + parent.getType() + ", baseUrl: " + parent.getXMLBaseURL());
			List<AuthorNode> childList = ((AuthorParentNode)parent).getContentNodes();
			int index = 1;
			for (AuthorNode child: childList) {
				if (child.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
					if (child == authorNode) {
						return index;
					}
					++index;
				}
			}
			return -1;
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
	public String evaluateXPathToString(String xPath) throws XPathException, XPathNotAvaliableException {
		checkXPathAvailable();
		
		try {
			Object[] result = authorAccess.getDocumentController().evaluateXPath(preprocessXPath(xPath), authorNode, false, false, false, false);
			if (result.length == 0) {
				return null;
			} else if (result.length == 1) {
				if (result[0] instanceof Node) {
					return ((Node)result[0]).getTextContent();
				} else {
					return (String)result[0];
				}
			} else {
				throw new XPathException("XPath expression ('" + xPath + "') doesn't return a single item.");
			}
		} catch (AuthorOperationException e){
			throw new XPathException("XPath expression ('" + xPath + "') failed to be evaluated: " + e.getMessage());
		}
	}

	@Override
	public NodeWrapper evaluateXPathToNode(String xPath) throws XPathException, XPathNotAvaliableException {

		checkXPathAvailable();
		
		try {
			AuthorNode[] result = authorAccess.getDocumentController().findNodesByXPath(preprocessXPath(xPath), authorNode, true, true, false, false);
			if ((result.length > 0) && (result[0] instanceof AuthorElement)) {
				return new AuthorNodeWrapper((AuthorNode)result[0], authorAccess);
			} else {
				/*logger.info("XXXXXXXXXXXXXXXXX " + xPath);
				if (authorNode.getParent() != null) {
					logger.info("parent: " + authorNode.getParent().getDisplayName());
					if (authorNode.getParent().getParent() != null) {
						logger.info("grandparent: " + authorNode.getParent().getParent().getDisplayName());
					}
				}*/
				throw new XPathException("XPath expression ('" + xPath + "') doesn't result in a single element. (" + result.length + ")");
			}
		} catch (AuthorOperationException e) {
			throw new XPathException("XPath expression ('" + xPath + "') failed to be evaluated.");
		}
		
	}

	@Override
	public List<NodeWrapper> evaluateXPathToNodeList(String xPath) throws XPathException, XPathNotAvaliableException {

		checkXPathAvailable();
		
		try {
			AuthorNode[] result = authorAccess.getDocumentController().findNodesByXPath(preprocessXPath(xPath), authorNode, true, true, false, false);
			if (result.length > 0) {
				final List<NodeWrapper> 	list 	= new LinkedList<>();
				for (int i = 0; i < result.length; ++i) {
					list.add(new AuthorNodeWrapper((AuthorNode)result[i], authorAccess));
				}
				return list;
			} else {
				return null;
			}
		} catch (AuthorOperationException e) {
			throw new XPathException("XPath expression ('" + xPath + "') failed to be evaluated.");
		}
	}

	@Override
	public List<String> evaluateXPathToStringList(String xPath) throws XPathException, XPathNotAvaliableException {
	
		checkXPathAvailable();

		final String preprocessed = preprocessXPath(xPath);
		
		try {
			final Object[] 		result 	= authorAccess.getDocumentController().evaluateXPath(preprocessed, authorNode, false, false, false, false);
			final List<String> 	list 	= new LinkedList<>();
			for (int i = 0; i < result.length; ++i) {
				if (result[i] instanceof Node) {
					list.add(((Node)result[i]).getTextContent());
				} else {
					list.add((String)result[i]);
				}
			}
			//logger.info("list to string: " + list.toString());
			//logger.info("array list to string: " + Arrays.toString(list.toArray()));
			return list;
		} catch (AuthorOperationException e){
			throw new XPathException("XPath expression ('" + xPath + "') failed to be evaluated." + (xPath.equals(preprocessed) ? "" : (" (preprocessed: '" + preprocessed + "')")));
		}
	}
	

	private String preprocessXPath(String xPath) throws XPathException, XPathNotAvaliableException {
		return OxyXPathHandler.getInstance().preprocessXPath(xPath, this);
	}

	public NodeWrapper getRootNode() {
		AuthorNode parent = authorNode;
		while (parent.getParent() != null) {
			parent = parent.getParent();
		}
		return new AuthorNodeWrapper(parent, authorAccess);
	}

	@Override
	public NodeWrapper getRootElement() {
		AuthorNode parent = authorNode;
		while ((parent.getParent() != null) && (parent.getParent().getType() != AuthorNode.NODE_TYPE_DOCUMENT)) {
			parent = parent.getParent();
		}
		return new AuthorNodeWrapper(parent, authorAccess);
	}

	@Override
	public boolean isText() {
		return false;
	}

	@Override
	public String getTextContent() {
		if (authorNode.getStartOffset() > 0) {
			try {
				return authorNode.getTextContent();
			} catch (BadLocationException e) {
				return "";
			}
		} else {
			return "";
		}
	}

	@Override
	public List<NodeWrapper> getChildNodes() {

		//logger.info("getChildNodes (" + authorNode.getDisplayName() + ")");
		
		List<NodeWrapper> childList = new LinkedList<NodeWrapper>();
		if (isElement()) {
			List<AuthorNode> nodeList = ((AuthorElement)authorNode).getContentNodes();
			if (authorAccess != null) {
				final AuthorDocumentController docController = authorAccess.getDocumentController();
				int prevEndOffset = authorNode.getStartOffset();
				for (AuthorNode child: nodeList) {
					if (child.getStartOffset() > prevEndOffset + 1) {	
						try {
							final Content 	content = docController.createDocumentFragment(prevEndOffset + 1, child.getStartOffset() - 1).getContent();
							final String	text	= content.getString(0,  content.getLength());
							childList.add(new AuthorTextNodeWrapper(text));
						} catch (BadLocationException e) {
							logger.error(e, e);
						}
					}
					childList.add(new AuthorNodeWrapper(child, authorAccess));
					prevEndOffset = child.getEndOffset();
				}
				if (authorNode.getEndOffset() > prevEndOffset + 1) {
					try {

						final Content 	content = docController.createDocumentFragment(prevEndOffset + 1, authorNode.getEndOffset() - 1).getContent();
						final String	text	= content.getString(0,  content.getLength());
						childList.add(new AuthorTextNodeWrapper(text));
					} catch (BadLocationException e) {
						logger.error(e, e);
					}
				}
			} else {
				// Text-Knoten können nicht generiert werden.
				for (AuthorNode child: nodeList) {
					childList.add(new AuthorNodeWrapper(child, authorAccess));
				}	
			}
		}
		
		return childList;
	}

	@Override
	public String getName() {
		return authorNode.getName();
	}
	
	public void setAttribute(String attrName, String value) {
		if (isElement()) {
			if (value != null) {
				((AuthorElement)authorNode).setAttribute(attrName, new AttrValue(value));
			} else {
				((AuthorElement)authorNode).removeAttribute(attrName);
			}
		}
	}

	public AuthorNode getAuthorNode() {
		return authorNode;
	}

	@Override
	public boolean isSameNode(NodeWrapper node) {
		if (node instanceof AuthorNodeWrapper) {
			final AuthorNode authorNode2 = ((AuthorNodeWrapper)node).getAuthorNode();
			return (authorNode.getStartOffset() == authorNode2.getStartOffset()) &&
					(authorNode.getXMLBaseURL().getPath().equals(authorNode2.getXMLBaseURL().getPath()));
		} else {
			return false;
		}
	}

	public void checkXPathAvailable() throws XPathException, XPathNotAvaliableException {
		if (authorAccess == null) {
			throw new XPathNotAvaliableException();
		}
		/*
		 *  In  Oxygen author mode in some cases (right after changes) the node can't be used as context for evaluating an xpath expression.
		 *  This can be noticed by checking if the xpath "." results in the node itself.
		 */
		final int docEndOffset 	= authorAccess.getDocumentController().getAuthorDocumentNode().getEndOffset();
		final int nodeEndOffset = authorNode.getStartOffset();
		if (nodeEndOffset > docEndOffset) {
			throw new XPathNotAvaliableException();
		} else {
			try {
				final AuthorNode[] results = authorAccess.getDocumentController().findNodesByXPath(".", authorNode, true, true, false, false);
				if ((results.length != 1) || (results[0].getStartOffset() != authorNode.getStartOffset())) {
					throw new XPathNotAvaliableException();
				}
			} catch (Exception e) {
				throw new XPathNotAvaliableException();
			}
		}
	}

}
