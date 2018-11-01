package org.DitaSemia.Base.AdvancedKeyref;

import java.util.List;

public interface KeyspecInterface {
	
	final static String TYPE_DELIMITER 	= ":";
	final static String PATH_DELIMITER 	= "/";
	final static String ID_DELIMITER	= "#";
	final static String ANY_NAMESPACE	= "*";
	final static String TYPE_UNDEFINED	= "*";
	
	

	String getKey();
	
	//String getText();

	String getType();

	String getNamespace();
	
	//String getPath();
	
	List<String> getNamespaceList();
	
	String getRefString();
}
