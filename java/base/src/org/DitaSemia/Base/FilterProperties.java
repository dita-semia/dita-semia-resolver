package org.DitaSemia.Base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;

public class FilterProperties {
	
	private static final Logger logger = Logger.getLogger(FilterProperties.class.getName());
	
	public static String[] FILTER_ATTR = {"audience", "platform", "product", "rev", "otherprops", "props"};
	
	protected static Set<String> filterAttrSet = null;

	protected Map<String, Set<String>> map;


	
	public FilterProperties() {
		map = null;
	}

	protected FilterProperties(Map<String, Set<String>> map) {
		this.map = map;
	}
	
	
	public static FilterProperties getFromNode(NodeWrapper node) {
		final Map<String, Set<String>> map = new HashMap<>();
		for (int i = 0; i < FILTER_ATTR.length; ++i) {
			final Set<String> set = stringToSet(node.getAttribute(FILTER_ATTR[i], null));
			if (set != null) {
				map.put(FILTER_ATTR[i], set);
			}
		}
		return new FilterProperties(map);
	}

	public static FilterProperties getFromNodeWithAncestors(NodeWrapper node) {
		final FilterProperties filterProperties = getFromNode(node);
		while (node.getParent() != null) {
			node = node.getParent();
			final FilterProperties ancestorFilterProperties = getFromNode(node);
			filterProperties.mergeWithAncestor(ancestorFilterProperties);
		}
		return filterProperties;
	}

	public boolean equals(FilterProperties other) {
		return ((map == null) && (other.map == null)) || (map.equals(other.map));
	}
	
	public void set(String filterKey, String prop) {
		if (map == null) {
			map = new HashMap<>();
		}
		final Set<String> set = stringToSet(prop);
		if (set != null) {
			map.put(filterKey, set);
		}
	}

	/*
	 * A reference is valid, if the destination is *not* more general than the reference.
	 */
	public static boolean isValidReference(FilterProperties ref, FilterProperties dest) {
		//logger.info("isValidReference(ref: " + ref + ", dest: " + dest + ")");
		
		if ((ref.map == null) || (dest.map == null)) {
			logger.error("isValidReference should not be called with undefined filter properties (ref: " + ref + ", dest: " + dest + ")");
			return false;
		}
		// every filter in destination needs to be set for reference as well.
		for (Entry<String, Set<String>> destEntry: dest.map.entrySet()) {
			final Set<String> refSet = ref.map.get(destEntry.getKey());
			if (refSet == null) {
				//logger.info("  missing filter for ref: '" + destEntry.getKey() + "' -> false");
				return false; 
			} else {
				if (!destEntry.getValue().containsAll(refSet)) {
					//logger.info("  missing values (" + String.join(" ", refSet) + ") for '" + destEntry.getKey() + "' in dest (" + String.join(" ", destEntry.getValue()) + ") -> false");
					return false;
				}
			}
		}
		//logger.info("  -> true");
		return true;
	}
	
	@Override
	public String toString() {
		if (isUndefined()) {
			return "<undefined>";
		} else if (map.isEmpty()) {
			return "<no restriction>";
		} else {
			final StringBuilder sb = new StringBuilder();
			for (Iterator<Entry<String, Set<String>>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			    final Entry<String, Set<String>> entry = iterator.next();
			    sb.append(entry.getKey());
			    sb.append("=\"");
			    sb.append(String.join(" ", entry.getValue()));
			    sb.append("\"");
			    if (iterator.hasNext()) {
			    	sb.append(" ");
			    }
			}
			return sb.toString();
		}
	}
	
	public void combine(FilterProperties other) {
		//logger.info("combine(" + this + ", " + other + ")");
		if (map == null) {
			if (other.map != null) {
				map = new HashMap<>(other.map);
			} else {
				// remain undefined
			}
		} else if (other.map != null) {
			for (Iterator<Entry<String, Set<String>>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
				final Entry<String, Set<String>> 	entry 		= iterator.next();
				final Set<String> 					otherSet 	= other.map.get(entry.getKey());
				if (otherSet == null) {
					iterator.remove();
				} else {
					entry.getValue().addAll(otherSet);
				}
			}
		} else {
			// no change
		}
		//logger.info("  -> " + this);
	}
	
	public boolean contains(String propKey, String value) {
		if (map != null) {
			final Set<String> set = map.get(propKey);
			if (set != null) {
				return set.contains(value);
			}
		}
		return false;
	}
	
	protected static Set<String> stringToSet(String prop) {
		if ((prop != null) && (!prop.isEmpty())) {
			final StringTokenizer 	stringTokenizer = new StringTokenizer(prop);
			final Set<String>		set				= new HashSet<>();
			while (stringTokenizer.hasMoreTokens()) {
				set.add(stringTokenizer.nextToken());
			}
			return set;
		} else {
			return null;
		}
	}

	protected void mergeWithAncestor(FilterProperties ancestorFilterProperties) {
		if (map == null) {
			if (ancestorFilterProperties.map != null) {
				map = new HashMap<>(ancestorFilterProperties.map);
			}
		} else {
			for (Entry<String, Set<String>> ancestorEntry: ancestorFilterProperties.map.entrySet()) {
				final Set<String> set = map.get(ancestorEntry.getKey());
				if (set != null) {
					// remove tokens missing in ancestor entry
					for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
					    final String token = iterator.next();
					    if (!ancestorEntry.getValue().contains(token)) {
					        iterator.remove();
					    }
					}
				} else {
					// property is inherited completely
					map.put(ancestorEntry.getKey(), ancestorEntry.getValue());
				}
			}
		}
	}


	public void writeToHddCache(XMLStreamWriter writer) throws XMLStreamException {
		if (map != null) {
			for (Entry<String, Set<String>> entry: map.entrySet()) {
				writer.writeAttribute(entry.getKey(), String.join(" ", entry.getValue()));
			}
		}
	}

	protected static Set<String> getFilterAttrSet() {
		if (filterAttrSet == null) {
			filterAttrSet = new HashSet<>();
			for (int i = 0; i < FILTER_ATTR.length; ++i) {
				filterAttrSet.add(FILTER_ATTR[i]);
			}
		}
		return filterAttrSet;
	}
	
	public static boolean isFilterAttribute(String name) {
		return getFilterAttrSet().contains(name);
	}

	public boolean isUndefined() {
		return (map == null);
	}

	public boolean isEmpty() {
		return ((map != null) && (map.isEmpty()));
	}

	public static FilterProperties createUnrestricted() {
		return new FilterProperties(new HashMap<>());
	}
	

}
