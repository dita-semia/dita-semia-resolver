/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Base;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.URIResolver;

import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NamespaceTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.Type;

import org.apache.log4j.Logger;

/**
 * 
 *
 */
public class SaxonNodeWrapper implements NodeWrapper
{
	private static final Logger logger = Logger.getLogger(SaxonNodeWrapper.class.getName());

	private final NodeInfo 		saxonNode;
	private final XPathCache 	xPathCache;
	
	/**
	 * Creates a SaxonNodeWrapper with the specified NodeInfo.
	 * 
	 * @param node the saxon node to be wrapped.
	 * @param xPathCache An instance of XPathCache with compatible configuration. Can be null unless an xpath is to be evaluated. 
	 */
	public SaxonNodeWrapper(NodeInfo node, XPathCache xPathCache) {
		this.saxonNode 	= node;
		this.xPathCache	= xPathCache;
	}

	
	/**
	 * Returns the private Field NodeInfo
	 * 
	 * @return NodeInfo
	 */
	public NodeInfo getNodeInfo() {
		return saxonNode;
	}
	
	@Override
	public URL getBaseUri() 
	{
		try
		{
			return new URL(saxonNode.getBaseURI());
		}
		catch (MalformedURLException e)
		{
			return null;
		}
	}

	@Override
	public boolean isElement() 
	{
		return (saxonNode != null) && (saxonNode.getNodeKind() == Type.ELEMENT);
	}

	@Override
	public String getAttribute(String localName, String namespaceUri) 
	{
		final String value = saxonNode.getAttributeValue(namespaceUri, localName); 
		//logger.info("getAttribute('" + namespace + "', '" + attrName + "'): '" + value + "'");
		return value;
	}

	@Override
	public NodeWrapper getParent() {
		if (saxonNode.getParent() != null) {
			return new SaxonNodeWrapper(saxonNode.getParent(), xPathCache);
		} else {
			return null;
		}
	}

	@Override
	public URIResolver getUriResolver() {
		return saxonNode.getConfiguration().getURIResolver();
	}

	public String serialize() {
		return serializeNode(saxonNode);
	}
	
	@Override
	public int getChildIndexWithinParent() {
		final NodeInfo parent = saxonNode.getParent();
		if (parent == null) {
			return -1;
		} else {
			final AxisIterator 	iterator 	= saxonNode.getParent().iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT);
			int index = 1;
			NodeInfo sibling = null;
			do {
				sibling = iterator.next();
				if (saxonNode.isSameNodeInfo(sibling)) {
					//logger.info("getChildIndexWithinParent(" + saxonNode.getLocalPart() + "): " + index);
					return index;
				}
				++index;
			}
			while (sibling != null);
		}
		return -1;
	}

	@Override
	public List<String> getAttributeNamesOfNamespace(String namespaceUri) {
		final List<String> list = new LinkedList<String>();
		if (isElement()) {
			final NodeTest 		nodeTest	= new NamespaceTest(saxonNode.getNamePool(), Type.ATTRIBUTE, namespaceUri);
			final AxisIterator 	iterator 	= saxonNode.iterateAxis(AxisInfo.ATTRIBUTE, nodeTest);
			NodeInfo 			attribute 	= iterator.next();
			while (attribute != null) {
				list.add(attribute.getLocalPart());
				attribute = iterator.next();
			}
		}
		return list;
	}
	
	/**
	 * Returns the serialised NodeInfo as String
	 * @param node NodeInfo to be serialised
	 * @return serialised NodeInfo
	 */
	public static String serializeNode(NodeInfo node) {
		try {
			return QueryResult.serialize(node);
		} catch (XPathException e) {
			logger.error("Error serializing node (" + node.getDisplayName() + "): "+ e.getMessage());
		}
		return null;
	}

	@Override
	public String evaluateXPathToString(String xPath) throws XPathException {
		//logger.info("getStringByXPath() SaxonNode");
		if (xPathCache == null) {
			throw new XPathException("SaxonNodeWrapper: Can't evaluate XPath ('" + xPath + "') without XPathCache.");
		} else {
			String resolvedXPath;
			try {
				XPathSelector selector = xPathCache.getXPathExecutable(xPath).load();
				//logger.info("XPathSelector: " + selector);
				selector.setContextItem(new XdmNode(saxonNode));
				final XdmItem resolvedItem = selector.evaluateSingle();
				if (resolvedItem != null) {
					resolvedXPath = selector.evaluateSingle().getStringValue();	
				} else {
					resolvedXPath = "";
				}
				//logger.info("resolvedXPath: " + resolvedXPath);
			} catch (SaxonApiException e) {
				throw new XPathException("XPath expression ('" + xPath + "') failed to be evaluated.");
			}
			return resolvedXPath;
		}
	}
}
