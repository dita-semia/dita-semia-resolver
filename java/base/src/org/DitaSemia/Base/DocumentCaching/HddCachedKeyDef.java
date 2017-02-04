package org.DitaSemia.Base.DocumentCaching;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.FilterAttrSet;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HddCachedKeyDef {
/*
	
	



	private String	key					= null;
	private String	type				= null;
	private String	namespace			= null;
	private String	name				= null;
	private String	desc				= null;
	private String	flags				= null;
	private String	refString			= null;
	private String	defId				= null;
	private String	defAncestorTopicId	= null;

	private final URL			defUrl;
	private final boolean		isRefExpected;
	private final boolean		isRefById;
	private final boolean		isFilteredKey;
	private final FilterAttrSet	filterAttrSet;

	public HddCachedKeyDef(URL defUrl, Attributes attributes) throws SAXException {
		
		final Map<String, Set<String>> filterAttrMap = new HashMap<>();
		
		this.defUrl = defUrl;
		
		for (int i = 0; i < attributes.getLength(); ++i) {
			if (attributes.getLocalName(i).equals(KeyDef.ATTR_KEY)) {
				key	= attributes.getValue(i);
			} else if (attributes.getLocalName(i).equals(KeyDef.ATTR_TYPE)) {
				type = attributes.getValue(i);
			} else {
				throw new SAXException("unexpected attribute '" + attributes.getLocalName(i) + "' on keydef.");
			}
		}

		isRefExpected 	= ((flags != null) && (flags.contains(KeyDef.FLAG_REF_EXPECTED)));
		isRefById		= ((flags != null) && (flags.contains(KeyDef.FLAG_REF_BY_ID)));
		isFilteredKey	= ((flags != null) && (flags.contains(KeyDef.FLAG_FILTERED_KEY)));

		filterAttrSet = new FilterAttrSet(filterAttrMap);
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
		return namespace;
	}

	@Override
	public List<String> getNamespaceList() {
		final String[] 		array 	= namespace.split(KeyDefInterface.PATH_DELIMITER);
		final List<String>	list	= new LinkedList<>();
		for (int i = 0; i < array.length; ++i) {
			list.add(array[i]);
		}
		return list;
	}

	@Override
	public String getRefString() {
		return refString;
	}

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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean matchesNamespaceFilter(List<String> namespaceFilter) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<NodeWrapper> getLinkedRefNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeWrapper getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeWrapper getNode() {
		// TODO Auto-generated method stub
		return null;
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
		return filterAttrSet;
	}

	@Override
	public String getFlags() {
		return flags;
	}
*/
}
