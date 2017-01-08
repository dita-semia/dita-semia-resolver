package org.DitaSemia.Base.AdvancedKeyref;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.saxon.trans.XPathException;

import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.XPathNotAvaliableException;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.apache.log4j.Logger;

public class KeyRef implements KeyRefInterface {

	private static final Logger logger = Logger.getLogger(KeyRef.class.getName());
	
	
	public 	static final String NAMESPACE_URI 			= "http://www.dita-semia.org/advanced-keyref";
	public 	static final String NAMESPACE_PREFIX 		= "akr"; 
	public 	static final String ATTR_REF				= "ref";
	public 	static final String ATTR_OUTPUTCLASS		= "outputclass";
	
	public  static final String OC_KEY					= "key";
	public  static final String OC_KEY_NAME_BRACED		= "key-name-braced";
	public  static final String OC_KEY_NAME_DASHED		= "key-name-dashed";
	public  static final String OC_NAME					= "name";
	
	private static final String ATTR_TYPE_FILTER		= "type";
	private static final String ATTR_NAMESPACE_FILTER 	= "namespace";
	private static final String ATTR_FIXED_PATH_LEN 	= "path-len";

	private static final String	OC_FIXED_MARKER			= "!";
	
	private static final String	UNKNOWN_NAME			= "???";
	
	

	private NodeWrapper 	node;
	private boolean			isInitDone		= false;
	private String 			type			= null;
	private String 			key				= null;
	private List<String> 	namespace		= null;
	
	public static KeyRef fromNode(NodeWrapper node) {
		final String refAttr = node.getAttribute(ATTR_REF, NAMESPACE_URI);
		if (refAttr != null) {
			return new KeyRef(node);	
		} else {
			return null;
		}
	}
	
	public KeyRef(NodeWrapper node) {
		this.node 	= node;	
	}
	
	public String getDisplaySuffix(BookCache cache, boolean showUnknownName) {
		return getDisplaySuffix(getMatchingKeyDef(cache), showUnknownName);
	}
	
	public String getDisplaySuffix(KeyDefInterface keyDef, boolean showUnknownName) {
		String name = (showUnknownName) ? UNKNOWN_NAME : null;
		if (keyDef != null) {
			name = keyDef.getName();
		}
		if ((name != null) && (!name.isEmpty())) {
			String outputclass = getOutputclass();
			if (outputclass.equals(OC_KEY_NAME_DASHED)) {
				// for outputclass "key" hiding the name will be done by css - but not in every case so provide it here.
				return " \u2013 " + name;
			} else if (outputclass.equals(OC_NAME)) {
				return name;
			} else {
				// for outputclass "key" hiding the name will be done by css - but not in every case so provide it here.
				return " (" + name + ")";	
			}
		} else {
			return "";
		}
	}
	
	public KeyDefInterface getMatchingKeyDef(BookCache cache) {
		return cache.getExactMatch(this);
	}
	
	
	@Override
	public URL getBaseUrl() {
		return node.getBaseUrl();
	}

	@Override
	public String getText() {
		return node.getTextContent();
	}
	
	private void init() {
		isInitDone = true;
		final String 	ref = node.getAttribute(ATTR_REF, NAMESPACE_URI);
		if (ref != null) {
			final String[] list = ref.split("[" + TYPE_DELIMITER + PATH_DELIMITER + "]");
			if (list.length > 0) {
				type = list[0];
				if (list.length > 1) {
					key = list[list.length -1];
					if (list.length > 2) {
						namespace = new LinkedList<>();
						for (int i = 1; i < list.length - 1; ++i) {
							namespace.add(list[i]);
						}
					}
				}
			}
		}
		if (type == null) {
			final Set<String> typeFilter = getTypeFilter();
			if ((typeFilter != null) && (typeFilter.size() == 1)) {
				type = typeFilter.iterator().next();
			}
		}
		//logger.info("ref:       " + ref);
		//logger.info("type:      " + type);
		//logger.info("key:       " + key);
		//logger.info("namespace: " + ((namespace == null) ? null : String.join(PATH_DELIMITER, namespace)));
	}
	
	@Override
	public String getKey() {
		if (!isInitDone) {
			init();
		}
		return key;
	}

	@Override
	public String getType() {
		if (!isInitDone) {
			init();
		}
		return type;
	}

	@Override
	public String getNamespace() {
		if (!isInitDone) {
			init();
		}
		return ((namespace == null) ? null : String.join(PATH_DELIMITER, namespace));
	}

	@Override
	public List<String> getNamespaceList() {
		if (!isInitDone) {
			init();
		}
		return namespace;
	}

	@Override
	public Set<String> getTypeFilter() {
		String typeFilter = node.getAttribute(ATTR_TYPE_FILTER, NAMESPACE_URI);
		if ((typeFilter != null) && (!typeFilter.isEmpty())) {
			String[] 	typeArray	= typeFilter.split("[\\s]+");
			Set<String> typeSet	= new HashSet<>(Arrays.asList(typeArray));
			return typeSet;
		} else {
			return null;
		}
	}

	@Override
	public List<String> getNamespaceFilter() {
		try {
			final String 		namespaceFilterXPath 	= node.getAttribute(ATTR_NAMESPACE_FILTER, NAMESPACE_URI);
			if ((namespaceFilterXPath == null) || (namespaceFilterXPath.isEmpty())) {
				return null;
			} else {
//				logger.info("getNamespaceFilter: " + namespaceFilterXPath);
//				logger.info("ergebnis: " + node.evaluateXPathToStringList(namespaceFilterXPath));
				return node.evaluateXPathToStringList(namespaceFilterXPath);
			}
		} catch (XPathException | XPathNotAvaliableException e) {
			logger.error(e, e);
			return null;
		}
	}

	@Override
	public int getFixedPathLen() {
		// TODO Auto-generated method stub
		String pathlen = node.getAttribute(ATTR_FIXED_PATH_LEN, NAMESPACE_URI); 
		if (pathlen == null) {
			return 0;
		} else {
			try {
				return Integer.parseInt(pathlen);
			} catch (NumberFormatException e) {
				//TODO
//				throw new XPathException("Invalid argument for path-len attribute ('" + pathlen + "').");
				return 0;
			}
		}
		
	}

//	@Override
//	public boolean isNamespaceFixed() {
//		// TODO Auto-generated method stub
//		return false;
//	}

	@Override
	public int getPathLen() {
		if (!isInitDone) {
			init();
		}
		int fixedPathLen = getFixedPathLen();
		if (fixedPathLen > 0) {
			return fixedPathLen;
		} else if (fixedPathLen == -1) {
			return namespace.size() + 1;
		} else {
			String path = getText();
			int startIndex = -1;
			for (String s : namespace) {
				if (path.startsWith(s)) {
					startIndex = namespace.indexOf(s);
				}
			}
			if (startIndex > -1) {
				return namespace.size() - startIndex + 1;
			} else {
				//only the key, no namespace elements
				return 1;
			}
		}
	}

	@Override
	public NodeWrapper getNode() {
		return node;
	}

//	@Override
//	public String getPath() {
//		// TODO Auto-generated method stub
//		logger.info("getPath(): type:namespace.key : " + getType() + ":" + getNamespace() + "." + getKey());
//		return getNamespace() + "." + getKey();
//	}

	@Override
	public String toString() {
		return "key = '" + getKey() + "'" +  
				", type = '" + getType() + "'" + 
				", mamespace = '" + getNamespace() + "'";
	}

	@Override
	public String getOutputclass() {
		String outputclass = node.getAttribute(ATTR_OUTPUTCLASS, null);
		if (isOutputclassFixed()) {
			outputclass = outputclass.substring(0, outputclass.length()-1);
		}
		return outputclass;
	}

	@Override
	public boolean isOutputclassFixed() {
		String outputclass = node.getAttribute(ATTR_OUTPUTCLASS, null);
		if (outputclass != null && !outputclass.isEmpty() && outputclass.endsWith(OC_FIXED_MARKER)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean matchesNamespaceFilter(List<String> namespaceFilter, List<String> namespace) {
		if ((namespace != null && !namespace.isEmpty()) && (namespaceFilter != null && !namespaceFilter.isEmpty())) {
			int nsFilterLength 	= namespaceFilter.size();
			int nsLength		= namespace.size();
			if (namespaceFilter.get(namespaceFilter.size() - 1).equals(KeyDef.ANY_NAMESPACE)) {
				//any subsidiary elements are allowed
				if (nsFilterLength > nsLength + 1) {
					return false;
				}
				for (int i = 0; i < nsFilterLength; i++) {
					if (!namespaceFilter.get(i).equals(KeyDef.ANY_NAMESPACE) && !namespaceFilter.get(i).equals(namespace.get(i))) {
						return false;
					}
				}
				return true;
			} else {
				//exact match
				for (int i = 0; i < nsFilterLength; i++) {
					if (!namespaceFilter.get(i).equals(namespace.get(i))) {
						return false;
					}
				}
				return true;
			}
		} else if ((namespace == null || namespace.isEmpty()) && (namespaceFilter != null && !namespaceFilter.isEmpty())) {
			return false;
		} else {
			return true;
		}
	}
	
	public static boolean matchesTypeFilter(Set<String> typeFilter, String type) {
		return (typeFilter == null) || (typeFilter.contains(type));
	}
}
