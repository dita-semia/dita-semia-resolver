package org.DitaSemia.Base.AdvancedKeyref;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.trans.XPathException;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.FilterAttrSet;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.XPathNotAvaliableException;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class KeyDef implements KeyDefInterface {

	private static final Logger logger = Logger.getLogger(KeyDef.class.getName());

	public 	static final String ATTR_TYPE		= "key-type";
	public 	static final String ATTR_ROOT		= "root";
	public 	static final String ATTR_REF_NODE	= "ref-node";
	public 	static final String ATTR_KEY 		= "key";
	public 	static final String ATTR_NAMESPACE	= "namespace";
	public 	static final String ATTR_NAME 		= "name";
	public 	static final String ATTR_DESC		= "desc";
	public 	static final String ATTR_ID			= "id";
	public 	static final String ATTR_FLAGS		= "flags";

	public  static final String FLAG_REF_EXPECTED	= "ref-expected";
	public  static final String FLAG_REF_BY_ID		= "ref-by-id";
	public  static final String FLAG_FILTERED_KEY	= "filtered-key";
	

	public static final String HC_KEYDEF			= "keydef";
	public static final String HC_KEY_FILTER		= "key-filter";
	
	public static final String HC_ATTR_DEF_ID		= "id";
	public static final String HC_ATTR_DEF_TOPIC_ID	= "topic-id";
	
	private final String 		key;
	private final String 		type;
	private final List<String> 	namespace;
	private final String 		desc;
	private final String 		name;
	/*private final NodeWrapper 	root;
	private final NodeWrapper 	node;*/
	private final URL			defUrl;
	private final String		defId;
	private final String		defAncestorTopicId;
	private final String		flags;
	private final boolean		isRefExpected;
	private final boolean		isRefById;
	private final boolean		isFilteredKey;

	private FilterAttrSet keyFilterAttrSet;

	public static KeyDef fromNode(NodeWrapper node) {
		return fromNode(node, null);
	}

	public static KeyDef fromNode(NodeWrapper node, String parentTopicId) {
//		logger.info("KeyDef fromNode");
		final String type = getTypeFromNode(node);
		if ((type != null) && (!type.isEmpty())) {
			try {
				return new KeyDef(node, type, parentTopicId);
			} catch (XPathException | XPathNotAvaliableException e) {
				logger.error(e, e);
				return null;
			}
		} else {
			return null;
		}
	}


	public static KeyDef fromHddCache(URL defUrl, Attributes attributes) throws SAXException {
		return new KeyDef(defUrl, attributes);
	}
	
	@Override
	public String getRefString() {
		if (isRefById) {
			return getType() + TYPE_DELIMITER + ID_DELIMITER + defId;
		} else {
			final String namespace = getNamespace();
			return getType() + TYPE_DELIMITER 
					+ ((namespace == null) || (namespace.isEmpty()) ? "" : (namespace + PATH_DELIMITER))
					+ getKey();
		}
	}

	public static String getTypeFromNode(NodeWrapper node) {
		return node.getAttribute(ATTR_TYPE, NAMESPACE_URI);
	}

	public static boolean nodeHasExplicitKey(NodeWrapper node) {
		return (node.getAttribute(ATTR_KEY, NAMESPACE_URI) != null);
	}

	private KeyDef(NodeWrapper node, String type, String parentTopicId) throws XPathException, XPathNotAvaliableException {
		//this.node 	= node;
		this.type	= type;
		//this.node	= node;

		final String 		rootAttr 	= node.getAttribute(ATTR_ROOT, NAMESPACE_URI);
		final NodeWrapper 	root 		= (rootAttr != null) ? node.evaluateXPathToNode(rootAttr) : node;
		
		if (root == null) {
			throw new XPathException("Missing root node.");
		}
		//this.root = root;
		

		final String 	refNodeAttr = node.getAttribute(ATTR_REF_NODE,	NAMESPACE_URI);
		NodeWrapper 	refNode		= (refNodeAttr != null) ? root.evaluateXPathToNode(refNodeAttr) : root;
		if (refNode == null) {
			refNode = root;
		}
		

		defUrl 	= refNode.getBaseUrl();
		defId	= refNode.getAttribute(ATTR_ID, null); 
		
		final String classAttr = root.getAttribute("class", null);
		if ((classAttr != null) && (classAttr.contains(" topic/topic "))) {
			defAncestorTopicId = null;
		} else if (parentTopicId != null) {
			defAncestorTopicId = parentTopicId;
		} else {
			NodeWrapper currentNode 	= refNode;
			String		currentClass;
			do {
				currentNode 	= currentNode.getParent();
				currentClass	= (currentNode == null) ? null : currentNode.getAttribute(DitaUtil.ATTR_CLASS, null);
			} while ((currentClass != null) && (!currentClass.contains(" topic/topic ")));
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

		flags 			= node.getAttribute(ATTR_FLAGS, NAMESPACE_URI);
		isRefExpected 	= ((flags != null) && (flags.contains(FLAG_REF_EXPECTED)));
		isRefById		= ((flags != null) && (flags.contains(FLAG_REF_BY_ID)));
		isFilteredKey	= ((flags != null) && (flags.contains(FLAG_FILTERED_KEY)));

		//logger.info("flags: " + flags + ", isFilteredKey: " + isFilteredKey);
		if ((isFilteredKey) && (keyAttr != null)) {
			final NodeWrapper keyNode = root.evaluateXPathToNode(keyAttr);
			//logger.info("keyNode: " + keyNode);
			if (keyNode == null) {
				keyFilterAttrSet = null;	// key will not be visible
			} else {
				// TODO: consider context as well
				keyFilterAttrSet = FilterAttrSet.getMerged(keyNode, null);
			}
		} else {
			keyFilterAttrSet = new FilterAttrSet();	// key will always be visible
		}
		//logger.info("keyFilterAttrSet (" + getRefString() + "): " + keyFilterAttrSet);
	}

	private KeyDef(URL defUrl, Attributes attributes) throws SAXException {
		//this.node	= null;
		//this.root	= null;
		this.defUrl = defUrl;
		
		String	key					= null;
		String	type				= null;
		String	namespace			= null;
		String	name				= null;
		String	desc				= null;
		String	flags				= null;
		String	defId				= null;
		String	defAncestorTopicId	= null;
		
		for (int i = 0; i < attributes.getLength(); ++i) {
			if (attributes.getLocalName(i).equals(ATTR_KEY)) {
				key	= attributes.getValue(i);
			} else if (attributes.getLocalName(i).equals(ATTR_TYPE)) {
				type = attributes.getValue(i);
			} else if (attributes.getLocalName(i).equals(ATTR_NAMESPACE)) {
				namespace = attributes.getValue(i);
			} else if (attributes.getLocalName(i).equals(ATTR_NAME)) {
				name = attributes.getValue(i);
			} else if (attributes.getLocalName(i).equals(ATTR_DESC)) {
				desc = attributes.getValue(i);
			} else if (attributes.getLocalName(i).equals(ATTR_FLAGS)) {
				flags = attributes.getValue(i);
			} else if (attributes.getLocalName(i).equals(HC_ATTR_DEF_ID)) {
				defId = attributes.getValue(i);
			} else if (attributes.getLocalName(i).equals(HC_ATTR_DEF_TOPIC_ID)) {
				defAncestorTopicId = attributes.getValue(i);
			} else {
				throw new SAXException("unexpected attribute '" + attributes.getLocalName(i) + "' on keydef.");
			}
		}

		this.key				= key;
		this.type				= type;
		this.namespace			= new LinkedList<>();;
		this.name				= name;
		this.desc				= desc;
		this.flags				= flags;
		this.defId				= defId;
		this.defAncestorTopicId	= defAncestorTopicId;
		
		if (namespace != null) {
			final String[] array = namespace.split(PATH_DELIMITER);
			for (int i = 0; i < array.length; ++i) {
				this.namespace.add(array[i]);
			}
		}

		isRefExpected 	= ((flags != null) && (flags.contains(FLAG_REF_EXPECTED)));
		isRefById		= ((flags != null) && (flags.contains(FLAG_REF_BY_ID)));
		isFilteredKey	= ((flags != null) && (flags.contains(FLAG_FILTERED_KEY)));

		//logger.info("keyFilterAttrSet (" + getRefString() + "): " + keyFilterAttrSet);
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
	public String getDefAncestorTopicId() {
		return defAncestorTopicId;
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

	/*@Override
	public NodeWrapper getRoot() {
		return root;
	}

	@Override
	public NodeWrapper getNode() {
		return node;
	}*/
	
	@Override
	public String toString() {
		return "key = '" + getKey() + "'" +
				", kype = '" + getType() + "'" + 
				", namespace = '" + getNamespace() + "'";
	}

	@Override
	public boolean isRefExpected() {
		return isRefExpected;
	}

	@Override
	public boolean isRefById() {
		return isRefById;
	}

	@Override
	public boolean isFilteredKey() {
		return isFilteredKey;
	}

	@Override
	public FilterAttrSet getKeyFilterAttrSet() {
		return keyFilterAttrSet;
	}

	@Override
	public String getFlags() {
		return flags;
	}

	@Override
	public void writeToHddCache(XMLStreamWriter writer) throws XMLStreamException {

		writer.writeCharacters("\n  ");
		writer.writeStartElement(HC_KEYDEF);

		writeAttribute(writer, ATTR_KEY, 				key);
		writeAttribute(writer, ATTR_TYPE, 				type);
		writeAttribute(writer, ATTR_NAMESPACE, 			getNamespace());
		writeAttribute(writer, ATTR_NAME, 				name);
		writeAttribute(writer, ATTR_DESC, 				desc);
		writeAttribute(writer, ATTR_FLAGS, 				flags);
		writeAttribute(writer, HC_ATTR_DEF_ID, 			defId);
		writeAttribute(writer, HC_ATTR_DEF_TOPIC_ID, 	defAncestorTopicId);
		
		if ((isFilteredKey) && (keyFilterAttrSet != null)){
			writer.writeCharacters("\n    ");
			writer.writeStartElement(HC_KEY_FILTER);
			Map<String, Set<String>> filterAttrMap = keyFilterAttrSet.getMap();
			if  (filterAttrMap != null) {
				for (Entry<String, Set<String>> entry : filterAttrMap.entrySet()) {
					writer.writeAttribute(entry.getKey(), String.join(" ", entry.getValue()));	
				}
			}	
			writer.writeEndElement();
			writer.writeCharacters("\n  ");
		}
		
		writer.writeEndElement();
	}
	
	
	private static void writeAttribute(XMLStreamWriter writer, String localName, String value) throws XMLStreamException {
		if (value != null) {
			writer.writeAttribute(localName, value);
		}
	}

	public void setKeyFilterFromHddCach(Attributes attributes) {
		// TODO Auto-generated method stub
		
	}

}
