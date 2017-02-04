package org.DitaSemia.Base.AdvancedKeyref;

import java.net.URL;
import java.util.List;


import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.DitaSemia.Base.FilterAttrSet;
import org.DitaSemia.Base.NodeWrapper;

public interface KeyDefInterface extends KeyspecInterface {

	public static final String 	NAMESPACE_URI		= "http://www.dita-semia.org/implicit-keydef";
	public static final String 	NAMESPACE_PREFIX	= "ikd";
	
	String 	getName();

	String 	getDesc();

	URL		getDefUrl();
	
	String 	getDefId();

	String getDefAncestorTopicId();

	String 	getDefLocation();

	// The key is not the text content but defined by an xpath expression
	//boolean isExplicitKey();

	//String getUniqueString();

	//boolean isOverwritable();

	//boolean matchesNamespace(String namespace, boolean isNamespaceFilter);

	//boolean matchesType(Set<String> typeList);

	//boolean matchesNamespaceSuffix(String namespaceSuffix);

	//boolean isKeyTypeOnlyExplicit();

	boolean matchesTypeFilter(Set<String> typeFilter);

	boolean matchesNamespaceFilter(List<String> namespaceFilter);

	List<NodeWrapper> getLinkedRefNodes();
	
	//NodeWrapper getRoot();

	//NodeWrapper getNode();
	
	boolean isRefExpected();

	boolean isRefById();

	boolean isFilteredKey();

	FilterAttrSet getKeyFilterAttrSet();

	String getFlags();

	void writeToHddCache(XMLStreamWriter writer) throws XMLStreamException;
}
