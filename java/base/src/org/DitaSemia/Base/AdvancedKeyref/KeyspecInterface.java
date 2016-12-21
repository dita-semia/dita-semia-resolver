package org.DitaSemia.Base.AdvancedKeyref;

import java.util.List;

public interface KeyspecInterface {
	
	final static String TYPE_DELIMITER 	= ":";
	final static String PATH_DELIMITER 	= "/";
	final static String ANY_NAMESPACE	= "*";
	

	String getKey();
	
	//String getText();

	String getType();

	String getNamespace();
	
	//String getPath();
	
	List<String> getNamespaceList();
	
	default public String getRefString() {
		final String namespace = getNamespace();
		return getType() + TYPE_DELIMITER 
				+ ((namespace == null) || (namespace.isEmpty()) ? "" : (namespace + PATH_DELIMITER))
				+ getKey();
	}
	
	default public boolean equals(KeyspecInterface keyspec) {
		return (getRefString().equals(keyspec.getRefString()));
	}
}
