package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

import java.util.HashSet;
import java.util.Set;

import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefListInterface;
import org.apache.log4j.Logger;

public class Common {
	
	private static final Logger logger = Logger.getLogger(Common.class.getName());

	public static KeyDefInterface GetAncestorKeyDef(NodeInfo contextNode, Sequence keyType, KeyDefListInterface keyDefList) throws XPathException {
		try {
			final Set<String> 		keyTypes = new HashSet<>();
			final SequenceIterator 	iterator = keyType.iterate();
			Item keyTypeItem = iterator.next();
			while (keyTypeItem != null) {
				keyTypes.add(((StringValue)keyTypeItem).getPrimitiveStringValue().toString());
				keyTypeItem = iterator.next();
			}

			final SaxonNodeWrapper	contextWrapper	= new SaxonNodeWrapper(contextNode, keyDefList.getXPathCache());

			//logger.info("Context Node: " + contextNode.getDisplayName());
			//logger.info("Context URL: " + contextNode.getBaseURI());

			return keyDefList.getAncestorKeyDef(contextWrapper, keyTypes);
		} catch (XPathException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e, e);
			return null;
		}
	}
	
}
