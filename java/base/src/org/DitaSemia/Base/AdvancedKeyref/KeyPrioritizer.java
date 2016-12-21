package org.DitaSemia.Base.AdvancedKeyref;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.DitaSemia.Base.NodeWrapper;
import org.apache.log4j.Logger;

public class KeyPrioritizer {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(KeyPrioritizer.class.getName());
	
	
	protected static final int CURRENT_REF_COEFFICIENT	= 5;	// coefficient used for the current reyref
	protected static final int ANCESTOR_DEF_COEFFICIENT	= 1;	// coefficient used for the ancestor keydef
	protected static final int CONTEXT_REF_COEFFICIENT	= 1;	// coefficient used for all reyrefs in the current context
	protected static final int LINKED_REF_COEFFICIENT	= 1;	// coefficient used for all reyrefs linked to a keydef referenced in the context

	protected static final int MATCHING_KEY					= 10;	// for complete matching key (same prefix will be weighted by it's length)
	protected static final int MATCHING_TYPE				= 4;	// for matching type
	protected static final int MATCHING_NAMESPACE_ELEMENT	= 2;	// score for each exact matching namespace element
	
	protected enum KeyRelationship { CURRENT_REF, ANCESTOR_DEF, CONTEXT_REF, LINKED_REF };

	protected static class ContextKey {
		final protected String			key;
		final protected String			type;
		final protected List<String>	namespace;
		final protected KeyRelationship	relationship;
		final protected int 			coefficient;
		final protected String 			keySuffix;

		ContextKey(final KeyspecInterface keyspec, final String key, final String keySuffix, final KeyRelationship relationship) {
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
			this. relationship = relationship; 
			coefficient	= (relationship == KeyRelationship.CURRENT_REF) 	? CURRENT_REF_COEFFICIENT 	: 
						  (relationship == KeyRelationship.ANCESTOR_DEF)	? ANCESTOR_DEF_COEFFICIENT 	:
						  (relationship == KeyRelationship.CONTEXT_REF)		? CONTEXT_REF_COEFFICIENT	: 
						  (relationship == KeyRelationship.LINKED_REF)		? LINKED_REF_COEFFICIENT	: 0;
			
			//logger.info("new ContextKey: '" + key + "', '" + type + "', '" + keyspec.getNamespace() + "', " + relationship);
		}

		int getPriority(KeyspecInterface keyspec) {
			int priority = 0;

			// key will be only used for current key
			if (relationship == KeyRelationship.CURRENT_REF) {
				final String specKey = keyspec.getKey();
				if ((key != null) && (key.length() > 0) && (specKey != null)) {
					final int minLength = Math.min(key.length(), specKey.length());
					int pos = 0;
					while ((pos < minLength) && (key.charAt(pos) == specKey.charAt(pos))) {
						++pos;
				    }
					priority += MATCHING_KEY * pos / Math.max(key.length(), specKey.length());
				}
			}

			if ((type != null) && (type.equals(keyspec.getType()))) {
				priority += MATCHING_TYPE;
			}

			final List<String> specNamespace = keyspec.getNamespaceList();
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
					}
				}
			}

			return priority * coefficient;
		}
	}
	
	protected List<ContextKey> contextKeyList = new LinkedList<>();

	public KeyPrioritizer(KeyDefListInterface keydefList, KeyRefInterface currentKeyref, KeyspecInterface ancestorKeydef, KeyrefFactory keyrefFactory) {
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
		
		if ((keySuffixes == null) || (keySuffixes.length == 1)) {
			contextKeyList.add(new ContextKey(currentKeyref, null, null, KeyRelationship.CURRENT_REF));
		} else {
			String key = keySuffixes[keySuffixes.length-1];
			String keySuffix = "";
			contextKeyList.add(new ContextKey(currentKeyref, key, null, KeyRelationship.CURRENT_REF));
			for (int i = keySuffixes.length - 2; i >= 0; i--) {
				if (keySuffix.equals("")) {
					keySuffix = keySuffixes[i];
				} else {
					keySuffix = keySuffixes[i] + delim + keySuffix;
				}
				contextKeyList.add(new ContextKey(currentKeyref, key, keySuffix, KeyRelationship.CURRENT_REF));
			}
		}
		
		if (ancestorKeydef != null) {
			contextKeyList.add(new ContextKey(ancestorKeydef, null, null, KeyRelationship.ANCESTOR_DEF));
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
							contextKeyList.add(new ContextKey(keyref, null, null, KeyRelationship.CONTEXT_REF));
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
										contextKeyList.add(new ContextKey(linkedKeyref, null, null, KeyRelationship.CONTEXT_REF));
									}
								}
							}
						}
					}
				}
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
