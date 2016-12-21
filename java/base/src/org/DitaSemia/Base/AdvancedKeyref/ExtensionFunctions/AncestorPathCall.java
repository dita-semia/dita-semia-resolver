package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import java.util.LinkedList;
import java.util.List;

import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefListInterface;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.StringValue;

public class AncestorPathCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(AncestorPathCall.class.getName());


	protected final KeyDefListInterface keyDefList;

	public AncestorPathCall(KeyDefListInterface keyDefList) {
		this.keyDefList	= keyDefList;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		
		try {
			
			final KeyDefInterface keyDef = Common.GetAncestorKeyDef(context, arguments, keyDefList);
			return getPath(keyDef);
			
		} catch (XPathException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException("Error: ", e.getMessage());
		}
	}

	public static Sequence getPath(KeyDefInterface keyDef) {
		if (keyDef != null) {
			List<String> 	path = keyDef.getNamespaceList();
			List<Item> 		list = new LinkedList<>();
			if (path != null) {
				for (String element : path) {
					list.add(new StringValue(element));
				}
			}
			list.add(new StringValue(keyDef.getKey()));
			//logger.info("result: " + keyDef.getNamespace() + " " + keyDef.getKey());
			return new SequenceExtent(list);
		} else {
			//logger.info("result: ()");
			return EmptySequence.getInstance();
		}
	}

}
