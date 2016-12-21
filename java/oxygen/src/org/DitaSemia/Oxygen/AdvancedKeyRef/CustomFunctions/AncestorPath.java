package org.DitaSemia.Oxygen.AdvancedKeyRef.CustomFunctions;

import java.util.List;

import org.DitaSemia.Base.DocumentCache;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.DocumentCacheHandler;
import org.DitaSemia.Oxygen.OxyXPathHandler;
import org.apache.log4j.Logger;


public class AncestorPath implements OxyXPathHandler.CustomFunction {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AncestorPath.class.getName());

	private static final int ARG_COUNT = 1;
	
	@Override
	public String getName() {
		return KeyDef.NAMESPACE_PREFIX + ":ancestor-path";
	}

	@Override
	public String evaluate(List<String> arguments, AuthorNodeWrapper context) {
		KeyDefInterface keyDef = null;
		if (context != null) {
			final DocumentCache documentCache = DocumentCacheHandler.getInstance().getDocumentCache(context.getBaseUrl());
			keyDef = documentCache.getAncestorKeyDef(context, arguments.get(0));
		} 

		if (keyDef == null) {
			return "()";
		} else {
			//logger.info("keyDef.getNamespace(): " + keyDef.getNamespace());
			List<String> path = keyDef.getNamespaceList();
			if (path == null) {
				return "'" + keyDef.getKey() + "'";
			} else {
				path.add(keyDef.getKey());
				//logger.info("result: " + String.join("/", path));
				return "('" + String.join("','", path) + "')";	
			}
		} 
	}

	@Override
	public int getArgCount() {
		return ARG_COUNT;
	}
}
