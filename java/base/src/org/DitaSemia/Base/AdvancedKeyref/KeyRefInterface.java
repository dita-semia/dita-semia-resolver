package org.DitaSemia.Base.AdvancedKeyref;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.DitaSemia.Base.NodeWrapper;
	

public interface KeyRefInterface extends KeyspecInterface {

	public static final String 	NAMESPACE_URI		= "http://www.dita-semia.org/advanced-keyref";
	public static final String 	NAMESPACE_PREFIX	= "akr";

	String getText();
	
	//	null: no limitation
	Set<String> getTypeFilter();

	List<String> getNamespaceFilter();
 
	// -1: complete path
	// 0: no predefined path length
	// 1: only the key without any namespace
	int getFixedPathLen();
	
	//boolean isNamespaceFixed();
	
	int getPathLen();

	NodeWrapper getNode();

	//String getPath();
	
	URL getBaseUrl();
	
	String getOutputclass();
	
	boolean isOutputclassFixed();
}
