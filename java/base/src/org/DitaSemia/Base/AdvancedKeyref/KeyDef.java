package org.DitaSemia.Base.AdvancedKeyref;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.saxon.trans.XPathException;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.XPathNotAvaliableException;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.apache.log4j.Logger;

public class KeyDef implements KeyDefInterface {

	private static final Logger logger = Logger.getLogger(KeyDef.class.getName());

	public static final String 	NAMESPACE_URI		= "http://www.dita-semia.org/implicit-keydef";
	public static final String 	NAMESPACE_PREFIX	= "ikd";

	public static final String 	ATTR_TYPE		= "key-type";
	private static final String ATTR_ROOT		= "root";
	private static final String ATTR_KEY 		= "key";
	private static final String ATTR_NAMESPACE	= "namespace";
	private static final String ATTR_NAME 		= "name";
	private static final String ATTR_DESC		= "desc";
	private static final String ATTR_ID			= "id";

	private final String 		key;
	private final String 		type;
	private final List<String> 	namespace;
	private final String 		desc;
	private final String 		name;
	private final NodeWrapper 	root;
	private final NodeWrapper 	node;
	private final URL			defUrl;
	private final String		defId;
	private final String		defAncestorTopicId;
	
	
	public static KeyDef fromNode(NodeWrapper node) {
//		logger.info("KeyDef fromNode");
		final String type = getTypeFromNode(node);
		if ((type != null) && (!type.isEmpty())) {
			try {
				return new KeyDef(node, type);
			} catch (XPathException | XPathNotAvaliableException e) {
				logger.error(e, e);
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static String getTypeFromNode(NodeWrapper node) {
		return node.getAttribute(ATTR_TYPE, NAMESPACE_URI);
	}
	
	public static boolean nodeHasExplicitKey(NodeWrapper node) {
		return (node.getAttribute(ATTR_KEY, NAMESPACE_URI) != null);
	}
	
	private KeyDef(NodeWrapper node, String type) throws XPathException, XPathNotAvaliableException {
		//this.node 	= node;
		this.type	= type;
		this.node	= node;

		final String 		rootAttr 	= node.getAttribute(ATTR_ROOT, NAMESPACE_URI);
		final NodeWrapper 	root 		= (rootAttr != null) ? node.evaluateXPathToNode(rootAttr) : node;
		
		if (root == null) {
			throw new XPathException("Missing root node.");
		}
		this.root = root;

		defUrl 	= root.getBaseUrl();
		defId	= root.getAttribute(ATTR_ID, null); 
		
		final String classAttr = root.getAttribute("class", null);
		if (classAttr.contains(" topic/topic ")) {
			defAncestorTopicId = null;
		} else {
			NodeWrapper currentNode = root;
			do {
				currentNode = currentNode.getParent();
			} while ((currentNode != null) && (!currentNode.getAttribute("class", null).contains(" topic/topic ")));
			if (currentNode != null) {
				defAncestorTopicId = currentNode.getAttribute(ATTR_ID, null);
			} else {
				defAncestorTopicId = null;
			}
		} 

		final String keyAttr 		= node.getAttribute(ATTR_KEY, 		NAMESPACE_URI);
		final String namespaceAttr 	= node.getAttribute(ATTR_NAMESPACE, NAMESPACE_URI);
		final String nameAttr 		= node.getAttribute(ATTR_NAME, 		NAMESPACE_URI);
		final String descAttr 		= node.getAttribute(ATTR_DESC, 		NAMESPACE_URI); 

		key 		= (keyAttr 			!= null) ? root.evaluateXPathToString(keyAttr) 			: node.getTextContent();
		namespace	= (namespaceAttr 	!= null) ? root.evaluateXPathToStringList(namespaceAttr): null;
		name		= (nameAttr 		!= null) ? root.evaluateXPathToString(nameAttr) 		: null;
		desc		= (descAttr 		!= null) ? root.evaluateXPathToString(descAttr)			: null;
	}
	
	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getNamespace() {
		return (namespace == null) ? null : String.join(PATH_DELIMITER, namespace);
	}

	@Override
	public List<String> getNamespaceList() {
		if (namespace == null || namespace.size() == 0) {
			return new LinkedList<String>();
		} else {
			return new LinkedList<String>(namespace);
		}
	}
	

	/*@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}*/

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDesc() {
		return desc;
	}

	@Override
	public URL getDefUrl() {
		return defUrl;
	}

	@Override
	public String getDefId() {
		return defId;
	}

	@Override
	public String getDefLocation() {
		if (defAncestorTopicId == null) {
			return FileUtil.decodeUrl(defUrl) + DitaUtil.HREF_URL_ID_DELIMITER + getDefId();
		} else {
			return FileUtil.decodeUrl(defUrl) + DitaUtil.HREF_URL_ID_DELIMITER + defAncestorTopicId + DitaUtil.HREF_TOPIC_ID_DELIMITER + getDefId();
		}
	}
	
	@Override
	public boolean matchesTypeFilter(Set<String> typeFilter) {
		return KeyRef.matchesTypeFilter(typeFilter, type);
	}

	@Override
	public boolean matchesNamespaceFilter(List<String> namespaceFilter) {
		return KeyRef.matchesNamespaceFilter(namespaceFilter, getNamespaceList());
	}

	@Override
	public List<NodeWrapper> getLinkedRefNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeWrapper getRoot() {
		return root;
	}
	
	@Override
	public String toString() {
		return "key = '" + getKey() + "'" +
				", kype = '" + getType() + "'" + 
				", namespace = '" + getNamespace() + "'";
	}

}
