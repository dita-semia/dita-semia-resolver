package org.DitaSemia.Base.AdvancedKeyref;

import java.net.URL;
import java.util.List;


import java.util.Set;

import org.DitaSemia.Base.NodeWrapper;

public interface KeyDefInterface extends KeyspecInterface {
	
	String 	getName();

	String 	getDesc();

	URL		getDefUrl();

	String 	getDefId();

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
	
	NodeWrapper getRoot();
}
