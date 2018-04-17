package org.DitaSemia.Base.AdvancedKeyref;

import java.net.URL;
import java.util.List;


import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.DitaSemia.Base.FilterAttrSet;
import org.DitaSemia.Base.FilterProperties;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.XslTransformerCacheProvider;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;

import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;

public interface KeyDefInterface extends KeyspecInterface {

	public static final String 	NAMESPACE_URI		= "http://www.dita-semia.org/implicit-keydef";
	public static final String 	NAMESPACE_PREFIX	= "ikd";
	

	public static final String 	DXD_NAMESPACE_URI		= "http://www.dita-semia.org/dynamic-xml-definition";
	public static final String 	DXD_NAMESPACE_PREFIX	= "dxd";
	
	String 	getName();

	String 	getDesc();

	URL		getDefUrl();
	
	String 	getDefId();

	String getDefAncestorTopicId();

	String 	getDefLocation();

	// The key is not the text content but defined by an xpath expression
	//boolean isExplicitKey();

	//String getUniqueString();

	//boolean matchesNamespace(String namespace, boolean isNamespaceFilter);

	//boolean matchesType(Set<String> typeList);

	//boolean matchesNamespaceSuffix(String namespaceSuffix);

	//boolean isKeyTypeOnlyExplicit();

	boolean matchesTypeFilter(Set<String> typeFilter);

	boolean matchesNamespaceFilter(List<String> namespaceFilter);
	
	int 	getMatchingElementsCount(List<String> namespaceFilter);

	List<NodeWrapper> getLinkedRefNodes();
	
	//NodeWrapper getRoot();

	//NodeWrapper getNode();

	boolean isOverwritable();

	boolean isResourceOnly();
	
	boolean isRefExpected();

	boolean isRefById();

	boolean isFilteredKey();

	boolean isDontLink();

	FilterAttrSet getKeyFilterAttrSet();

	String getFlags();

	void writeToHddCache(XMLStreamWriter writer) throws XMLStreamException;
	
	String getDxdTypeName();
	
	String getDxdTypeXsl();

	Sequence getDxdTypeDef(XslTransformerCacheProvider xslTransformerCacheProvider, BookCacheProvider bookCacheProvider) throws XPathException;

	//String getDxdValueRegExp();
	
	FilterProperties getFilterProperties();
}
