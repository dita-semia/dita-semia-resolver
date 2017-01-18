package org.DitaSemia.Base.AdvancedKeyref;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.XPathCache;

public interface KeyDefListInterface {

	public Collection<KeyDefInterface> getKeyDefs();

	public KeyDefInterface getExactMatch(KeyspecInterface keyspec);
	
	public KeyDefInterface getExactMatch(String refString);

	public KeyDefInterface getAncestorKeyDef(NodeWrapper node, String keyType);

	public XPathCache getXPathCache();

	public KeyTypeDef getKeyTypeDef(String type);
	
	public Collection<KeyDefInterface> getMatchingKeyDefs(Set<String> typeFilter, List<String> namespaceFilter);
	
}
