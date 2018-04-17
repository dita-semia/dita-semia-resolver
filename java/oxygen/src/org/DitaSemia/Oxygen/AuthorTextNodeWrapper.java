package org.DitaSemia.Oxygen;

import java.net.URL;
import java.util.List;

import javax.xml.transform.URIResolver;

import net.sf.saxon.trans.XPathException;

import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.XPathNotAvaliableException;


public class AuthorTextNodeWrapper implements NodeWrapper {

	private String text;
	
	AuthorTextNodeWrapper(String text) {
		this.text = text;
	}
	
	@Override
	public String getName() {
		return "#text";
	}

	@Override
	public String serialize() {
		return text;
	}

	@Override
	public NodeWrapper getRootNode() {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public NodeWrapper getRootElement() {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public URL getBaseUrl() {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public boolean isElement() {
		return false;
	}

	@Override
	public boolean isText() {
		return true;
	}
	
	@Override
	public String getTextContent() {
		return text;
	}

	@Override
	public List<NodeWrapper> getChildNodes() {
		return null;
	}

	@Override
	public NodeWrapper getParent() {
		throw new RuntimeException("Not implemented.");
	}

	/*@Override
	public void setAttribute(String attrName, String value) {
		throw new NotImplementedException();
	}*/

	@Override
	public String getAttribute(String localName, String namespaceUri) {
		return null;
	}

	@Override
	public URIResolver getUriResolver() {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public int getChildElementIndexWithinParent() {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public List<String> getAttributeNamesOfNamespace(String namespaceUri) {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public String evaluateXPathToString(String xPath) throws XPathException {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public NodeWrapper evaluateXPathToNode(String xPath) throws XPathException {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public List<String> evaluateXPathToStringList(String namespaceAttr) throws XPathException, XPathNotAvaliableException {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public boolean isSameNode(NodeWrapper node) {
		return false;
	}

	@Override
	public List<NodeWrapper> evaluateXPathToNodeList(String xPath) throws XPathException, XPathNotAvaliableException {
		throw new RuntimeException("Not implemented.");
	}

}
