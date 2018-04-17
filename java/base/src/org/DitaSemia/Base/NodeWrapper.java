/*
 * org.DitaSemia.JavaBase.NodeWrapper
 * 
 * v0.0.2beta
 * 
 * 2015-11-11
 * 
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Base;

import java.net.URL;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.log4j.Logger;

import net.sf.saxon.trans.XPathException;

/**
 * The NodeWrapper interface is designed for the handling of Nodes in the context of the DITA-SEMIA project.
 *  
 * @version 0.0.2beta
 */

public interface NodeWrapper
{		
	/**
	 * Returns the absolute base URL of the Node or null if there is none.
	 * 
	 * @return the base URL of the xml source or null 
	 */
	public URL getBaseUrl();

	/**
	 * Checks whether the Type of the Node is Element.
	 * 
	 * @return true if the Type is Element, false if it's not or if the Node is null.
	 */
	public boolean isElement();

	/**
	 *  Returns the value of the requested attribute or null if the attribute does not exists
	 *  
	 * @param attrName Name of the requested attribute
	 * @param namespaceUri URI of the namespace, null if there is no namespace.
	 * @return the value of the attribute, null if the attribute is empty (null?)
	 */
	public String getAttribute(String localName, String namespaceUri);
	
	/**
	 * Returns a NodeWrapper of the parent of the Node, null if the parent is null (root?)
	 * 
	 * @return the parent Node or null if there is none
	 */
	public NodeWrapper getParent();

	/**
	 * Returns an <a href="https://docs.oracle.com/javase/7/docs/api/javax/xml/transform/URIResolver.html">URIResolver</a> in the current context of the Node.
	 * 
	 * @return an {@link javax.xml.transform.URIResolver}
	 */
	public URIResolver getUriResolver();

	/**
	 * Returns the serialized Node as XML
	 * 
	 * @return a String of the Nodes XML code
	 * @throws XPathException 
	 */
	public String serialize() throws XPathException;

	/**
	 * Returns the Index of the Node within the content of its parent.
	 * 
	 * @return the index of the Node, 1 if the parent is a Document, -1 if there is no parent.
	 */
	// Was bedeutet es, wenn parent ein document ist?
	public int getChildElementIndexWithinParent();
	
	/**
	 * Returns a List of Strings with the local name of every attribute that has the given namespace-prefix
	 * 
	 * @param namespaceUri the URI of the namespace
	 * @return a List of Strings with the local attribute names, an empty list if there are none
	 */
	public List<String> getAttributeNamesOfNamespace(String namespaceUri);
	
	/**
	 * Resolves the specifiedxpPath expression.
	 * 
	 * @param xPath
	 * @return the evaluated xpath expression.
	 */
	public String evaluateXPathToString(String xPath) throws XPathException, XPathNotAvaliableException;

	public List<String> evaluateXPathToStringList(String namespaceAttr) throws XPathException, XPathNotAvaliableException;

	NodeWrapper getRootNode();

	NodeWrapper getRootElement();

	boolean isText();

	String getTextContent();

	List<NodeWrapper> getChildNodes();

	NodeWrapper evaluateXPathToNode(String xPath) throws XPathException, XPathNotAvaliableException;

	List<NodeWrapper> evaluateXPathToNodeList(String xPath) throws XPathException, XPathNotAvaliableException;

	public String getName();

	boolean isSameNode(NodeWrapper node);
	

	default public Source resolveUri(String uri) throws TransformerException {
		return getUriResolver().resolve(uri, getBaseUrl().getPath());
	}
}
