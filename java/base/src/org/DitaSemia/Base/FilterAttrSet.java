package org.DitaSemia.Base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import java.util.Set;

public class FilterAttrSet {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FilterAttrSet.class.getName());
	
	public static String FILTER_ATTR_LIST[] = {"audience", "product", "platform"};
	
	final protected Map<String, Set<String>>	map;

	public FilterAttrSet() {
		map = null;
	}
	
	protected FilterAttrSet(Map<String, Set<String>> map) {
		this.map = map;
	}

	public static FilterAttrSet getMerged(NodeWrapper node, FilterAttrSet parentFilterSet) {
		Map<String, Set<String>> localMap = getLocalMap(node);
		if ((parentFilterSet == null) || (parentFilterSet.map == null)) {
			// no parent filtering -> return local map
			return new FilterAttrSet(localMap);
		} else if (localMap == null) {
			// no change -> return parent
			return parentFilterSet;
		} else {
			Map<String, Set<String>> map = new HashMap<>();
			for (int i = 0; i < FILTER_ATTR_LIST.length; ++i) {
				final Set<String> set = getMergedSet(localMap.get(FILTER_ATTR_LIST[i]), parentFilterSet.map.get(FILTER_ATTR_LIST[i]));
				if (set != null) {
					map.put(FILTER_ATTR_LIST[i], set);
				}
			}
			return new FilterAttrSet(map);
		}
	}
	
	public Map<String, Set<String>> getMap() {
		return map;
	}
	
	protected static Set<String> getSingleSet(NodeWrapper node, String attrName) {
		final String attr = node.getAttribute(attrName, null);
		//logger.info("getSingleSet(" + attrName + "): " + attr);
		if ((attr == null) || (attr.isEmpty())) {
			return null;
		} else {
			final String[] 		list 	= attr.split("\\s+");
			final Set<String>	set		= new HashSet<>();
			for (int i = 0; i < list.length; ++i) {
				//logger.info("  " + list[i]);
				set.add(list[i]);
			}
			return set;
		}
	}
	
	protected static Map<String, Set<String>> getLocalMap(NodeWrapper node) {
		Map<String, Set<String>> map = null;
		for (int i = 0; i < FILTER_ATTR_LIST.length; ++i) {
			final Set<String> set = getSingleSet(node, FILTER_ATTR_LIST[i]);
			if (set != null) {
				if (map == null) {
					map = new HashMap<>();
				}
				map.put(FILTER_ATTR_LIST[i], set);
			}
		}
		return map;
	}
	
	protected static Set<String> getMergedSet(Set<String> localSet, Set<String> parentSet) {
		if (localSet == null) {
			return parentSet;
		} else if (parentSet == null) {
			return localSet;
		} else {
			// return intersection
			final Set<String> set = new HashSet<>();
			for (String token : localSet) {
				if (parentSet.contains(token)) {
					set.add(token);
				}	
			}
			return set;
		}
	}
	
	@Override
	public String toString() {
		if (map == null) {
			return "<no filter>";
		} else {
			final StringBuffer buffer = new StringBuffer();
			for (Entry<String, Set<String>> entry : map.entrySet()) {
				buffer.append(entry.getKey());
				buffer.append(": [");
				for (String token : entry.getValue()) {
					buffer.append(token);
					buffer.append(" ");
				}
				buffer.append("] ");
			}
			return buffer.toString();
		}
	}
	
}
