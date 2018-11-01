package org.DitaSemia.Oxygen.AdvancedKeyRef.CustomFunctions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.BookCacheHandler;
import org.DitaSemia.Oxygen.OxyXPathHandler;
import org.DitaSemia.Oxygen.OxyXPathHandler.Argument;
import org.DitaSemia.Oxygen.OxyXPathHandler.ArgumentType;
import org.apache.log4j.Logger;


public class AncestorPath implements OxyXPathHandler.CustomFunction {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AncestorPath.class.getName());
	
	@Override
	public String getName() {
		return KeyDef.NAMESPACE_PREFIX + ":ancestor-path";
	}

	@Override
	public String evaluate(List<Argument> arguments, AuthorNodeWrapper context) {
		KeyDefInterface keyDef = null;
		if (context != null) {
			final BookCache bookCache = BookCacheHandler.getInstance().getBookCache(context.getBaseUrl());
			final Set<String> keyTypes = new HashSet<>();
			keyTypes.addAll(arguments.get(0).stringList);
			keyDef = (bookCache != null ? bookCache.getAncestorKeyDef(context, keyTypes) : null);
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
	public ArgumentType[] getArgumentTypes() {
		final ArgumentType[] argumentTypes = {ArgumentType.STRING_LIST};
		return argumentTypes;
	}

}
