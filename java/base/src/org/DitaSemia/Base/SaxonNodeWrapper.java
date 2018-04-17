/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Base;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NamespaceTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
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
	
	private static final Properties serializationProperties = getSerializationProperties();
	
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

	
	private static Properties getSerializationProperties() {
		Properties properties = new Properties();
		properties.setProperty(OutputKeys.INDENT, 					"no");
		properties.setProperty(OutputKeys.OMIT_XML_DECLARATION, 	"yes");
		return properties;
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
	public URL getBaseUrl() {
		try {
			// workaround for bug with xinclude and subfolders by evaluating ancestor-or-self::*/@xml:base
			NodeInfo 	ancestor			= saxonNode;
			String 		xmlBaseAttribute 	= null;
			do {
				xmlBaseAttribute 	= ancestor.getAttributeValue("http://www.w3.org/XML/1998/namespace", "base");
				ancestor 			= ancestor.getParent();
			}
			while ((ancestor != null) && (xmlBaseAttribute == null));
			if (xmlBaseAttribute != null) {
				//logger.info("getBaseUrl: " + ancestor.getBaseURI() + ", " + xmlBaseAttribute + " -> " + new URL(new URL(ancestor.getBaseURI()), xmlBaseAttribute));
				return new URL(new URL(ancestor.getBaseURI()), xmlBaseAttribute);
			} else {
				return new URL(saxonNode.getBaseURI());
			}
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public boolean isElement() {
		return (saxonNode != null) && (saxonNode.getNodeKind() == Type.ELEMENT);
	}

	@Override
	public String getAttribute(String localName, String namespaceUri) {
		final String value = saxonNode.getAttributeValue((namespaceUri == null) ? "" : namespaceUri, localName); 
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
	public int getChildElementIndexWithinParent() {
		final NodeInfo parent = saxonNode.getParent();
		if (parent == null) {
			return -1;
		} else {
			final AxisIterator 	iterator 	= saxonNode.getParent().iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT);
			int index = 1;
			NodeInfo sibling = null;
			do {
				sibling = iterator.next();
				if (saxonNode.getNodeKind() == Type.ELEMENT) {
					if (saxonNode.isSameNodeInfo(sibling)) {
						//logger.info("getChildIndexWithinParent(" + saxonNode.getLocalPart() + "): " + index);
						return index;
					}
					++index;
				}
			}
			while (sibling != null);
		}
		return -1;
	}

	@Override
	public List<String> getAttributeNamesOfNamespace(String namespaceUri) {
		final List<String> list = new LinkedList<String>();
		if (isElement()) {
			final NodeTest 		nodeTest	= new NamespaceTest(saxonNode.getConfiguration().getNamePool(), Type.ATTRIBUTE, namespaceUri);
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
			final StringWriter writer = new StringWriter();
			final StreamResult result = new StreamResult(writer);
			QueryResult.serialize(node, result, serializationProperties);
			return writer.getBuffer().toString();
		} catch (XPathException e) {
			logger.error("Error serializing node (" + node.getDisplayName() + "): "+ e.getMessage());
		}
		return null;
	}

	@Override
	public String evaluateXPathToString(String xPath) throws XPathException {
		final XdmItem resolvedItem = evaluateXPathToItem(xPath, null);
		if (resolvedItem != null) {
			return  resolvedItem.getStringValue();	
		} else {
			return "";
		}
	}

	@Override
	public NodeWrapper evaluateXPathToNode(String xPath) throws XPathException {
		final XdmItem resolvedItem = evaluateXPathToItem(xPath, null);
		if (resolvedItem != null) {
			if (resolvedItem instanceof XdmNode) {
				return new SaxonNodeWrapper(((XdmNode)resolvedItem).getUnderlyingNode(), xPathCache);	
			} else {
				throw new XPathException("XPath expression ('" + xPath + "') results in a not-node item.");		
			}	
		} else {
			return null;
		}
	}


	@Override
	public List<NodeWrapper> evaluateXPathToNodeList(String xPath) throws XPathException, XPathNotAvaliableException {
		final XdmValue resolvedValue = evaluateXPathToValue(xPath, null);
		if ((resolvedValue != null) && (resolvedValue.size() > 0)) {
			List<NodeWrapper> list = new LinkedList<>();
			for (XdmItem item : resolvedValue) {
				//logger.info("item: " + item);
				if (item instanceof XdmNode) {
					list.add(new SaxonNodeWrapper(((XdmNode)item).getUnderlyingNode(), xPathCache));
				}
			}
			return list;
		} else {
			return null;
		}
	}

	@Override
	public List<String> evaluateXPathToStringList(String xPath) throws XPathException {
		return evaluateXPathToStringList(xPath, null);
	}
	
	public List<String> evaluateXPathToStringList(String xPath, Map<QName, XdmValue> variableMap) throws XPathException {
		final XdmValue resolvedValue = evaluateXPathToValue(xPath, variableMap);
		if (resolvedValue != null) {
			List<String> list = new LinkedList<>();
			for (XdmItem item : resolvedValue) {
				list.add(item.getStringValue());
			}
			return list;
		} else {
			return null;
		}
	}
	
	private XdmItem evaluateXPathToItem(String xPath, Map<QName, XdmValue> variableMap) throws XPathException {
		try {
			return getXPathSelector(xPath, variableMap).evaluateSingle();	
		} catch (SaxonApiException e) {
			throw new XPathException("Failed to evaluate XPath expression: '" + xPath + "'): " + e.getMessage());
		}
	}

	private XdmValue evaluateXPathToValue(String xPath, Map<QName, XdmValue> variableMap) throws XPathException {
		try {
			return getXPathSelector(xPath, variableMap).evaluate();
		} catch (SaxonApiException e) {
			throw new XPathException("Failed to evaluate XPath expression: '" + xPath + "'): " + e.getMessage());
		}
	}
	
	private XPathSelector getXPathSelector(String xPath, Map<QName, XdmValue> variableMap) throws XPathException, SaxonApiException {
		if (xPathCache == null) {
			throw new XPathException("SaxonNodeWrapper: Can't evaluate XPath ('" + xPath + "') without XPathCache.");
		} else {
			return xPathCache.getXPathSelector(xPath, new XdmNode(saxonNode), variableMap);
		}
	}


	@Override
	public NodeWrapper getRootNode() {
		return new SaxonNodeWrapper(saxonNode.getDocumentRoot(), xPathCache);
	}

	@Override
	public NodeWrapper getRootElement() {
		final AxisIterator 	iterator 	= saxonNode.getDocumentRoot().iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT);
		return new SaxonNodeWrapper(iterator.next(), xPathCache);
	}


	@Override
	public boolean isText() {
		return (saxonNode != null) && (saxonNode.getNodeKind() == Type.TEXT);
	}


	@Override
	public String getTextContent() {
		return saxonNode.getStringValue();
	}


	@Override
	public List<NodeWrapper> getChildNodes() {
		final AxisIterator 		iterator 	= saxonNode.iterateAxis(AxisInfo.CHILD);
		final List<NodeWrapper>	list		= new LinkedList<>();
		NodeInfo childNode = iterator.next();
		while (childNode != null) {
			list.add(new SaxonNodeWrapper(childNode, xPathCache));
			childNode = iterator.next();
		}
		return list;
	}


	@Override
	public String getName() {
		return saxonNode.getLocalPart();
	}


	@Override
	public boolean isSameNode(NodeWrapper node) {
		return ((node instanceof SaxonNodeWrapper) && (saxonNode.isSameNodeInfo(((SaxonNodeWrapper)node).getSaxonNode())));
	}


	private NodeInfo getSaxonNode() {
		return saxonNode;
	}
}
