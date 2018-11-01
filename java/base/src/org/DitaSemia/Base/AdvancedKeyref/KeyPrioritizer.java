package org.DitaSemia.Base.AdvancedKeyref;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.DitaSemia.Base.NodeWrapper;
import org.apache.log4j.Logger;

public class KeyPrioritizer {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(KeyPrioritizer.class.getName());
	
	
	protected static final int CURRENT_REF_COEFFICIENT	= 500;	// coefficient used for the current reyref
	protected static final int ANCESTOR_DEF_COEFFICIENT	= 100;	// coefficient used for the ancestor keydef
	protected static final int CONTEXT_REF_COEFFICIENT	= 100;	// coefficient used for all reyrefs in the current context
	protected static final int LINKED_REF_COEFFICIENT	= 100;	// coefficient used for all reyrefs linked to a keydef referenced in the context
	protected static final int HISTORY_REF_COEFFICIENT	=  10;	// coefficient used for all previously selected keys (weighted by their index) 

	protected static final int MATCHING_KEY						= 10;	// for complete matching key (same prefix will be weighted by it's length)
	protected static final int MATCHING_TYPE					= 4;	// for matching type
	protected static final int MATCHING_NAMESPACE_ELEMENT		= 2;	// score for each exact matching namespace element
	protected static final int NONMATCHING_NAMESPACE_ELEMENT	= 1;	// score for each exact non-matching namespace element (will be substracted)
	
	protected enum KeyRelationship { CURRENT_REF, ANCESTOR_DEF, CONTEXT_REF, LINKED_REF, HISTORY_REF };

	protected static class ContextKey {
		final protected String			key;
		final protected String			type;
		final protected List<String>	namespace;
		final protected KeyRelationship	relationship;
		final protected int 			coefficient;
		final protected String 			keySuffix;
		
		public static ContextKey createCurrentRef(final KeyspecInterface keyspec, final String key, final String keySuffix) {
			return new ContextKey(keyspec, key, keySuffix, KeyRelationship.CURRENT_REF, CURRENT_REF_COEFFICIENT);
		}
		
		public static ContextKey createAncestorKeydef(final KeyspecInterface keydef) {
			return new ContextKey(keydef, null, null, KeyRelationship.ANCESTOR_DEF, ANCESTOR_DEF_COEFFICIENT);
		}

		public static ContextKey createContextRef(KeyRefInterface keyref) {
			return new ContextKey(keyref, null, null, KeyRelationship.CONTEXT_REF, CONTEXT_REF_COEFFICIENT);
		}

		public static ContextKey createHistoryRef(final KeyspecInterface keyref, final int historyIndex, final int maxHistoryIndex) {
			return new ContextKey(keyref, null, null, KeyRelationship.HISTORY_REF, HISTORY_REF_COEFFICIENT * (maxHistoryIndex - historyIndex) / maxHistoryIndex);
		}

		private ContextKey(final KeyspecInterface keyspec, final String key, final String keySuffix, final KeyRelationship relationship, final int coefficient) {
			// the key will only be used to prioritize the current keyref
			if (key != null) {
				this.key = key;
			} else {
				this.key = keyspec.getKey();
			}
			type					= keyspec.getType();
			this.keySuffix			= keySuffix;
			final List<String> list = keyspec.getNamespaceList();
			if (list == null) {
				namespace = new LinkedList<>();
			} else {
				namespace = new LinkedList<>(list);
			}
			if (relationship != KeyRelationship.CURRENT_REF) {
				namespace.add(keyspec.getKey());
			}
			this.relationship 	= relationship;
			this.coefficient 	= coefficient;
			
//			logger.info("new ContextKey: '" + key + "', '" + type + "', '" + keyspec + "', " + relationship);
		}

		int getPriority(KeyspecInterface keyspec) {
			int priority = 0;
			
			final List<String> specNamespace = keyspec.getNamespaceList();

			// key and end of namespace will be only used for current key
			if (relationship == KeyRelationship.CURRENT_REF) {
				final String specKey = keyspec.getKey();
				if ((key != null) && (key.length() > 0) && (specKey != null)) {
					final int minLength = Math.min(key.length(), specKey.length());
					int pos = 0;
					while ((pos < minLength) && (key.charAt(pos) == specKey.charAt(pos))) {
						++pos;
				    }
					priority += MATCHING_KEY * pos / Math.max(key.length(), specKey.length());
					//logger.info("matching key " + key + "/" + specKey + " - priority: " + priority + ", pos: " + pos + ", length: " + Math.max(key.length(), specKey.length()));
				}
				if ((specNamespace != null) && (namespace != null)) {	
					//logger.info("    specNamespace: " + String.join("/", specNamespace) + ", namespace: " + String.join("/", namespace));
					ListIterator<String> defNamespaceIt = specNamespace.listIterator(specNamespace.size());
					ListIterator<String> locNamespaceIt = namespace.listIterator(namespace.size());
					while ((defNamespaceIt.hasPrevious()) && (locNamespaceIt.hasPrevious())) {
						final String defNamespace = defNamespaceIt.previous();
						final String locNamespace = locNamespaceIt.previous();
						//logger.info("     compare: " + defNamespace + " / " + locNamespace);
						if (defNamespace.equals(locNamespace)) {
							priority += MATCHING_NAMESPACE_ELEMENT;
							//logger.info(" *" + defNamespace);
						} else {
							break;
						}
					}
				}
			}

			if ((type != null) && (type.equals(keyspec.getType()))) {
				priority += MATCHING_TYPE;
			}

			if ((specNamespace != null) && (namespace != null)) {
				//logger.info("compare namespaces: '" + String.join("/",  namespace) + "', '" + keyspec.getNamespace() + "'"); 
				Iterator<String> defNamespaceIt = specNamespace.iterator();
				Iterator<String> locNamespaceIt = namespace.iterator();
				while ((defNamespaceIt.hasNext()) && (locNamespaceIt.hasNext())) {
					final String defNamespace = defNamespaceIt.next();
					final String locNamespace = locNamespaceIt.next();
					if (defNamespace.equals(locNamespace)) {
						priority += MATCHING_NAMESPACE_ELEMENT;
						//logger.info(" *" + defNamespace);
					} else {
						priority -= 2 * NONMATCHING_NAMESPACE_ELEMENT;
						break;
					}
				}
				while (defNamespaceIt.hasNext()) {
					defNamespaceIt.next();
					priority -= NONMATCHING_NAMESPACE_ELEMENT;
				}
				while (locNamespaceIt.hasNext()) {
					locNamespaceIt.next();
					priority -= NONMATCHING_NAMESPACE_ELEMENT;
				}
			}
			
			//logger.info("relationship: " + relationship + ": " + keyspec.getRefString() + " - " + type + ", " + String.join("/", namespace) + ", " + key + "  : " + priority  + "x" + coefficient);

			return priority * coefficient;
		}
	}
	
	protected List<ContextKey> contextKeyList = new LinkedList<>();

	public KeyPrioritizer(KeyDefListInterface keydefList, KeyRefInterface currentKeyref, KeyspecInterface ancestorKeydef, KeyrefFactory keyrefFactory, List<KeyspecInterface> historyList) {
		assert (currentKeyref == null) : "The current Keyref is null.";		
		
		char 		delim		= '\0';
		String 		keyrefText 	= currentKeyref.getText();
		String[] 	keySuffixes = null;
		if (keyrefText != null && keyrefText.contains(".")) {
			delim 		= '.';
			keySuffixes = keyrefText.split("\\.");
		} else if (keyrefText != null && keyrefText.contains("/")) {
			delim 		= '/';
			keySuffixes = keyrefText.split("\\/");
		}
		
		if (keySuffixes == null) {
			if (!keyrefText.isEmpty()) {
				contextKeyList.add(ContextKey.createCurrentRef(currentKeyref, keyrefText, null));
			}
		} else {
			String key = keySuffixes[keySuffixes.length-1];
			String keySuffix = "";
			contextKeyList.add(ContextKey.createCurrentRef(currentKeyref, key, null));
			for (int i = keySuffixes.length - 2; i >= 0; i--) {
				if (keySuffix.equals("")) {
					keySuffix = keySuffixes[i];
				} else {
					keySuffix = keySuffixes[i] + delim + keySuffix;
				}
				contextKeyList.add(ContextKey.createCurrentRef(currentKeyref, key, keySuffix));
			}
		}
		
		if (ancestorKeydef != null) {
			contextKeyList.add(ContextKey.createAncestorKeydef(ancestorKeydef));
		}
		
		final NodeWrapper node = currentKeyref.getNode();
		if ((node != null) && (keyrefFactory != null)) {
			final NodeWrapper parent = node.getParent();
			if (parent != null) {
				List<NodeWrapper> siblings = parent.getChildNodes();
				for (NodeWrapper sibling: siblings) {
					if (!sibling.isSameNode(currentKeyref.getNode())) {
						final KeyRefInterface keyref = keyrefFactory.createKeyref(sibling);
						if (keyref != null) {
							contextKeyList.add(ContextKey.createContextRef(keyref));
							KeyDefInterface keydef = null;
							if (keydefList != null) {
								keydef = keydefList.getExactMatch(keyref);
							}
							if (keydef != null) {
								//logger.info("keydef: " +keydef.getKey());
								final List<NodeWrapper> linkedList = keydef.getLinkedRefNodes();
								for (NodeWrapper linkedNode: linkedList) {
									/*try {
										logger.info("  linked node: " + linkedNode.serialize());
									} catch (XPathException e) {
									}*/
									final KeyRefInterface linkedKeyref = keyrefFactory.createKeyref(linkedNode);
									if (linkedKeyref != null) {
										//logger.info("  linked keyref: " + linkedKeyref.getKey());
										contextKeyList.add(ContextKey.createContextRef(linkedKeyref));
									}
								}
							}
						}
					}
				}
			}
		}
		
		if (historyList != null) {
			final int 	size 	= historyList.size();
			int 		i 		= 0;
			for (KeyspecInterface key : historyList) {
				contextKeyList.add(ContextKey.createHistoryRef(key, i, size));
				++i;
			}
		}
		// TODO: consider keyrefs in other listitems / tablecells
		// TODO: filter duplicates
	}

	public int getPriority(KeyspecInterface keyspec) {
		int priority = 0;

		for (ContextKey contextKey : contextKeyList) {
			priority += contextKey.getPriority(keyspec);
		}

		return priority;
	}

}
