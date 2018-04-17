package org.DitaSemia.Base.AdvancedKeyref;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class KeyDefTableModelEntry {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(KeyDefTableModelEntry.class.getName());
	
	private final KeyDefInterface keyDef;
	
	private final int 			priority;
	private final String 		key;
	private final String 		type;
	private final List<String> 	namespace;
	private final String		name;
	private final String		desc;
	private final String		defUrl;
	private final String		defId;
	private final String		filterProperties;

	public KeyDefTableModelEntry (	KeyDefInterface keyDef,
									int priority,
									String key, 
									String type, 
									List<String> namespace, 
									String name, 
									String desc, 
									String defUrl, 
									String defId,
									String filterProperties) {
		
		this.keyDef					= keyDef;
		
		this.priority				= priority;
		this.key 					= key;
		this.type					= type;
		this.namespace				= namespace;
		this.name					= name;
		this.desc					= desc;
		this.defUrl					= defUrl;
		this.defId					= defId;
		this.filterProperties		= filterProperties;
		
	}
	
	public static KeyDefTableModelEntry fromKeyDef(KeyDefInterface keyDef, int priority) {
		
		//TODO...
		String 			key 		= keyDef.getKey();
		String 			type 		= keyDef.getType();
		List<String> 	namespace 	= keyDef.getNamespaceList();
		String 			name 		= keyDef.getName();
		String 			desc 		= keyDef.getDesc();
		String 			defUrl 		= keyDef.getDefUrl().getPath();
		String 			defId 		= keyDef.getDefId();
		String 			filterProps = keyDef.getFilterProperties().toString();
		
		return new KeyDefTableModelEntry(keyDef, priority, key, type, namespace, name, desc, defUrl, defId, filterProps);
	}
	
	/** 
	 * 
	 * Returns the String value of the desired field.
	 * 0 = priority; 1 = key; 2 = name; 3 = type; 4 = namespace; 5 = description; 6 = defUri; 7 = defId;
	 * 
	 */
	
	public int getPriority() {
		return priority;
	}
	
	public String get(int index) {
		switch (index) {
		case 0:
			//TODO aufsplitten für Prio
			break;
		case 1:
			return key;
		case 2:
			return name;
		case 3: 
			return type;
		case 4: 
			return getNamespace();
		case 5: 
			return desc;
		case 6:
			return defUrl;
		case 7:
			return defId;
		case 8: 
			return filterProperties;
		default: 
			throw new IllegalArgumentException("Index must be a number between 0 and 7");
		}
		return null;
	}
	
	public KeyDefInterface getKeyDef() {
		return keyDef;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getType() {
		return type;
	}
	
	public List<String> getNamespaceList() {
		if (namespace != null) {
			return namespace;
		} else {
			return new ArrayList<String>();
		}
	}
	
	public String getNamespace() {
		if (namespace != null && !namespace.isEmpty()) {
			return String.join(KeyspecInterface.PATH_DELIMITER,  namespace);
		} else {
			return null;
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getDesc() {
		return desc;
	}

	public String getDefUrl() {
		return defUrl;
	}

	public String getDefId() {
		return defId;
	}
	
	public String getFilterProperties() {
		return filterProperties;
	}
	
	public String getUniqueString() {
		return key + "#" + type + "#" + namespace;
	}
	
	@Override
	public String toString() {
		return "Key: " + key + ", Type: " + type + ", Name: " + name + ", Namespace: " + this.getNamespace() + ", Description: " + desc;
	}

	public static class Comparator implements java.util.Comparator<KeyDefTableModelEntry> {

		public int compare(KeyDefTableModelEntry keyDef1, KeyDefTableModelEntry keyDef2) {
			return keyDef1.getUniqueString().compareToIgnoreCase(keyDef2.getUniqueString());
		}
		
	}
}
