package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import java.util.LinkedList;
import java.util.List;

import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.apache.log4j.Logger;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.StringValue;

public class GetPathCall extends KeyDefExtensionFunctionCall {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GetPathCall.class.getName());


	@Override
	public Sequence call(KeyDefInterface keyDef) {
		return getPath(keyDef);
	}

	public static Sequence getPath(KeyDefInterface keyDef) {
		//logger.info("getPath: " + keyDef);
		if (keyDef != null) {
			final List<String> 	path = keyDef.getNamespaceList();
			final List<Item> 	list = new LinkedList<>();
			if (path != null) {
				for (String element : path) {
					//logger.info("  - " + element);
					list.add(new StringValue(element));
				}
			}
			list.add(new StringValue(keyDef.getKey()));
			//logger.info("  - " + keyDef.getKey());
			//logger.info("result: " + keyDef.getNamespace() + " " + keyDef.getKey());
			return new SequenceExtent(list);
		} else {
			//logger.info("result: ()");
			return EmptySequence.getInstance();
		}
	}

}
