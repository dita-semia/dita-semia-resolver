package org.DitaSemia.Base.AdvancedKeyref;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;

import org.DitaSemia.Base.ConbatResolver;
import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.FilterAttrSet;
import org.DitaSemia.Base.FilterProperties;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.XPathNotAvaliableException;
import org.DitaSemia.Base.XslTransformerCache;
import org.DitaSemia.Base.XslTransformerCacheProvider;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.DitaSemia.Base.DynamicXmlDefinition.DxdCodeblockResolver;
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
	
	public 	static final String ATTR_DXD_TYPE_XSL	= "type-xsl";
	public 	static final String ATTR_DXD_TYPE_NAME	= "type-name";

	public  static final String FLAG_REF_EXPECTED	= "ref-expected";
	public  static final String FLAG_REF_BY_ID		= "ref-by-id";
	public  static final String FLAG_FILTERED_KEY	= "filtered-key";
	public  static final String FLAG_DONT_LINK		= "dont-link";
	

	public static final String HC_KEYDEF			= "keydef";
	public static final String HC_KEY_FILTER		= "key-filter";
	
	public static final String HC_ATTR_DEF_ID		= "id";
	public static final String HC_ATTR_DEF_TOPIC_ID	= "topic-id";
	
	public static final String HC_ATTR_DXD_TYPE_XSL		= "dxd-type-xsl";
	public static final String HC_ATTR_DXD_TYPE_NAME	= "dxd-type-name";
	
	private final String 			key;
	private final String 			type;
	private final List<String> 		namespace;
	private final String 			desc;
	private final String 			name;
	private final URL				defUrl;
	private final String			defId;
	private final String			defAncestorTopicId;
	private final String			flags;
	private final boolean			isResourceOnly;
	private final boolean			isOverwritable;
	private final boolean			isRefExpected;
	private final boolean			isRefById;
	private final boolean			isFilteredKey;
	private final boolean			isDontLink;
	private final FilterProperties 	filterProperties;
	
	private final String 			dxdTypeXsl;
	private final String 			dxdTypeName;

	private FilterAttrSet 			keyFilterAttrSet;
	
	private Sequence				dxdTypeDef = null;

	public static KeyDef fromNode(NodeWrapper node) {
		return fromNode(node, false, null);
	}

	public static KeyDef fromNode(NodeWrapper node, boolean isResourceOnly, String parentTopicId) {
		//logger.info("KeyDef fromNode");
		final String type = getTypeFromNode(node);
		if ((type != null) && (!type.isEmpty())) {
			try {
				return new KeyDef(node, type, isResourceOnly, parentTopicId);
			} catch (XPathException | XPathNotAvaliableException e) {
				logger.error(e, e);
				return null;
			}
		} else {
			return null;
		}
	}


	public static KeyDef fromHddCache(URL defUrl, Attributes attributes, boolean isResourceOnly) throws SAXException {
		return new KeyDef(defUrl, attributes, isResourceOnly);
	}
	
	@Override
	public String getRefString() {
		if (isRefById) {
			return escapeString(getType()) + TYPE_DELIMITER + ID_DELIMITER + defId;
		} else {
			return createRefString(getType(), getNamespaceList(), getKey());
		}
	}
	
	public static String createRefString(String type, List<String> namespaceList, String key) {
		final StringBuilder result = new StringBuilder();
		
		result.append(escapeString(type));
		result.append(TYPE_DELIMITER);
		if (namespaceList != null) {
			for (String entry : namespaceList) {
				result.append(escapeString(entry));
				result.append(PATH_DELIMITER);
			}
		}
		if (key != null) {
			result.append(escapeString(key));
		}
		
		return result.toString();
	}
	
	private static String escapeString(String str) {
		return (str != null && !str.isEmpty()) ? str.replaceAll("([.:/])", "\\\\$1") : str;
	}

	public static String getTypeFromNode(NodeWrapper node) {
		return node.getAttribute(ATTR_TYPE, NAMESPACE_URI);
	}

	public static boolean nodeHasExplicitKey(NodeWrapper node) {
		return (node.getAttribute(ATTR_KEY, NAMESPACE_URI) != null);
	}

	private KeyDef(NodeWrapper node, String type, boolean isResourceOnly, String parentTopicId) throws XPathException, XPathNotAvaliableException {
		this.type			= type;
		this.isResourceOnly	= isResourceOnly;
		this.isOverwritable	= isResourceOnly;
		
		//logger.info("parentTopicId: " + parentTopicId);

		final String 		rootAttr 	= node.getAttribute(ATTR_ROOT, NAMESPACE_URI);
		final NodeWrapper 	root 		= (rootAttr != null) ? node.evaluateXPathToNode(rootAttr) : node;
		
		if (root == null) {
			throw new XPathException("Missing root node.");
		}

		this.filterProperties	= FilterProperties.getFromNodeWithAncestors(root);

		final String 	refNodeAttr = node.getAttribute(ATTR_REF_NODE,	NAMESPACE_URI);
		NodeWrapper 	refNode		= (refNodeAttr != null) ? root.evaluateXPathToNode(refNodeAttr) : root;
		if (refNode == null) {
			refNode = root;
		}
		

		defUrl 	= refNode.getBaseUrl();
		defId	= refNode.getAttribute(ATTR_ID, null); 
		
		final String refNodeClassAttr = refNode.getAttribute("class", null);
		//logger.info("refNodeClassAttr: " + refNodeClassAttr);
		if ((refNodeClassAttr != null) && (refNodeClassAttr.contains(" topic/topic "))) {
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

		key 		= (keyAttr 			!= null) ? normalizeSpace(root.evaluateXPathToString(keyAttr)) 	: normalizeSpace(ConbatResolver.getResolvedContent(node));
		namespace	= (namespaceAttr 	!= null) ? root.evaluateXPathToStringList(namespaceAttr)		: null;
		name		= (nameAttr 		!= null) ? normalizeSpace(root.evaluateXPathToString(nameAttr)) : null;
		desc		= (descAttr 		!= null) ? normalizeSpace(root.evaluateXPathToString(descAttr))	: null;

		flags 			= node.getAttribute(ATTR_FLAGS, NAMESPACE_URI);
		isRefExpected 	= ((flags != null) && (flags.contains(FLAG_REF_EXPECTED)) && (!isResourceOnly));
		isRefById		= ((flags != null) && (flags.contains(FLAG_REF_BY_ID)));
		isFilteredKey	= ((flags != null) && (flags.contains(FLAG_FILTERED_KEY)));
		isDontLink		= ((flags != null) && (flags.contains(FLAG_DONT_LINK)));

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
		

		final String dxdTypeNameAttr 	= node.getAttribute(ATTR_DXD_TYPE_NAME, DXD_NAMESPACE_URI);
		dxdTypeName = (dxdTypeNameAttr != null) ? normalizeSpace(node.evaluateXPathToString(dxdTypeNameAttr))	: null;
		dxdTypeXsl	= node.getAttribute(ATTR_DXD_TYPE_XSL, DXD_NAMESPACE_URI);
	}
	
	private KeyDef(URL defUrl, Attributes attributes, boolean isResourceOnly) throws SAXException {
		//this.node	= null;
		//this.root	= null;
		this.defUrl 			= defUrl;
		this.isResourceOnly		= isResourceOnly;
		this.isOverwritable		= isResourceOnly;
		this.filterProperties 	= FilterProperties.createUnrestricted();
		
		String	key					= null;
		String	type				= null;
		String	namespace			= null;
		String	name				= null;
		String	desc				= null;
		String	flags				= null;
		String	defId				= null;
		String	defAncestorTopicId	= null;
		String	dxdTypeName			= null;
		String	dxdTypeXsl			= null;
		
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
			} else if (attributes.getLocalName(i).equals(HC_ATTR_DXD_TYPE_NAME)) {
				dxdTypeName = attributes.getValue(i);
			} else if (attributes.getLocalName(i).equals(HC_ATTR_DXD_TYPE_XSL)) {
				dxdTypeXsl = attributes.getValue(i);
			} else if (FilterProperties.isFilterAttribute(attributes.getLocalName(i))) { 
				filterProperties.set(attributes.getQName(i), attributes.getValue(i));
			} else {
				throw new SAXException("unexpected attribute '" + attributes.getLocalName(i) + "' on keydef.");
			}
		}
		

		this.key				= key;
		this.type				= type;
		this.namespace			= new LinkedList<>();
		this.name				= name;
		this.desc				= desc;
		this.flags				= flags;
		this.defId				= defId;
		this.defAncestorTopicId	= defAncestorTopicId;
		this.dxdTypeName		= dxdTypeName;
		this.dxdTypeXsl			= dxdTypeXsl;
		
		if ((namespace != null) && (!namespace.isEmpty())) {
			final String[] array = namespace.split(PATH_DELIMITER);
			for (int i = 0; i < array.length; ++i) {
				this.namespace.add(array[i]);
			}
		}

		isRefExpected 	= ((flags != null) && (flags.contains(FLAG_REF_EXPECTED)) && (!isResourceOnly));
		isRefById		= ((flags != null) && (flags.contains(FLAG_REF_BY_ID)));
		isFilteredKey	= ((flags != null) && (flags.contains(FLAG_FILTERED_KEY)));
		isDontLink		= ((flags != null) && (flags.contains(FLAG_DONT_LINK)));

		//logger.info("keyFilterAttrSet (" + getRefString() + "): " + keyFilterAttrSet);
	}

	/*private void dxdInit(NodeWrapper node, NodeWrapper root) throws XPathException, XPathNotAvaliableException {

		final String attrDxdChildList	= node.getAttribute(ATTR_DXD_CHILD_LIST, DXD_NAMESPACE_URI);
		
		if (attrDxdChildList != null) {
			List<NodeWrapper> childList = root.evaluateXPathToNodeList(attrDxdChildList);
			if ((childList != null) && (!childList.isEmpty())) {
				dxdChildRefStringList = new ArrayList<String>();
				for (NodeWrapper child : childList) {
					final KeyRef childKeyRef = KeyRef.fromNode(child);
					if (childKeyRef != null) {
						dxdChildRefStringList.add(childKeyRef.getRefString());
					} else {
						final KeyDef childKeyDef = KeyDef.fromNode(child);
						if (childKeyDef != null) {
							dxdChildRefStringList.add(childKeyDef.getRefString());	
						}
					}
				}
				logger.info("dxdChildRefStringList (" + key + "): " + String.join(" ", dxdChildRefStringList));
			}
		} else {
			dxdChildRefStringList = null;
		}
	}*/
	
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
	public int getMatchingElementsCount(List<String> namespaceFilter) {
		int count 			= 0;
		int nsLength 		= ((namespace == null) ? 0 : namespace.size());
		int nsFilterLength 	= ((namespaceFilter == null) ? 0 : namespaceFilter.size());
		for (int i = 0; ((i < nsLength) && (i < nsFilterLength)); i++) {
			if (namespace.get(i).equals(namespaceFilter.get(i))) {
				count ++;
			} else {
				break;
			}
		}
		return count;
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
				", type = '" + getType() + "'" + 
				", namespace = '" + getNamespace() + "'" +
				", refString = '" + getRefString() + "'";
	}

	@Override
	public boolean isOverwritable() {
		return isOverwritable;
	}

	@Override
	public boolean isResourceOnly() {
		return isResourceOnly;
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
	public boolean isDontLink() {
		return isDontLink;
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
		writeAttribute(writer, HC_ATTR_DXD_TYPE_NAME, 	dxdTypeName);
		writeAttribute(writer, HC_ATTR_DXD_TYPE_XSL, 	dxdTypeXsl);

		this.filterProperties.writeToHddCache(writer);
		
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

	
	public static String normalizeSpace(String string) {
		if (string != null) {
			return string.replaceAll("\\s+", " ").replaceAll("^\\s|\\s$", "");
		} else {
			return null;
		}
	}

	@Override
	public String getDxdTypeName() {
		return dxdTypeName;
	}

	@Override
	public String getDxdTypeXsl() {
		return dxdTypeXsl;
	}

	@Override
	public Sequence getDxdTypeDef(XslTransformerCacheProvider xslTransformerCacheProvider, BookCacheProvider bookCacheProvider) throws XPathException {
		if ((dxdTypeDef == null) && (dxdTypeXsl != null)) {
			//logger.info("getDxdTypeDef: " + toString());
			final XslTransformerCache 	transformerCache 	= xslTransformerCacheProvider.getXslTransformerCache();
			//logger.info("  transformerCache: " + transformerCache);
			final XsltExecutable 		executable			= transformerCache.getExecutable(dxdTypeXsl);
			final XsltTransformer 		xslTransformer 		= executable.load();

			final BookCache 		bookCache	= bookCacheProvider.getBookCache(defUrl);
			if (bookCache == null) {
				return EmptySequence.getInstance();
			}
			final String			defLocation	= getDefLocation();
			//logger.info("  defLocation: " + defLocation);
			final SaxonNodeWrapper	keyDefRoot	= (SaxonNodeWrapper)bookCache.getNodeByLocation(defLocation);
			//logger.info("  keyDefRoot: " + keyDefRoot);
			xslTransformer.setInitialContextNode(new XdmNode(keyDefRoot.getNodeInfo()));

			xslTransformer.setParameter(DxdCodeblockResolver.PARAM_TYPE_NAME, 	XdmValue.wrap(new StringValue(dxdTypeName)));
			xslTransformer.setParameter(DxdCodeblockResolver.PARAM_DEF_URL, 	XdmValue.wrap(new AnyURIValue(defUrl.toExternalForm())));

			final XdmDestination destination = new XdmDestination();
			xslTransformer.setDestination(destination);
			
			try {
				xslTransformer.transform();
			} catch (SaxonApiException e) {
				throw new XPathException("Runtime Error. " + e.getMessage());
			}
			
			final AxisIterator 	childIterator 	= destination.getXdmNode().getUnderlyingNode().iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT);
			final NodeInfo 		defElement 		= childIterator.next();
			
			if (defElement != null) {
				dxdTypeDef = defElement;	// return 1st (and only) child element
			} else {
				logger.warn("No type definition returned by xsl script '" + dxdTypeXsl + "' for key '" + getRefString() + "'");
				dxdTypeDef = EmptySequence.getInstance();
			}
		}
		return dxdTypeDef;
	}

	@Override
	public FilterProperties getFilterProperties() {
		return filterProperties;
	}

}
