package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef.DisplaySuffix;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.KeyDefExtensionFunctionCall;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.StringValue;

public class GetDisplaySuffixCall extends KeyDefExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(GetDisplaySuffixCall.class.getName());

	protected final BookCacheProvider 			bookCacheProvider;
	
	public GetDisplaySuffixCall(BookCacheProvider bookCacheProvider) {
		this.bookCacheProvider	= bookCacheProvider;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {

		final Sequence 	node	= arguments[0].head();

		try {
			if (!(node instanceof NodeInfo)) {
				throw new XPathException("Supplied 1st parameter '" + node + "' is no compatible node.");
			}
			
			final NodeInfo 		nodeInfo 	= (NodeInfo)node;
			final BookCache		bookCache	= bookCacheProvider.getBookCache(new URL(nodeInfo.getBaseURI()));
			final NodeWrapper 	keyRefNode	= new SaxonNodeWrapper(nodeInfo, bookCache.getXPathCache());
			final KeyRef 		keyRef		= KeyRef.fromNode(keyRefNode);
			final Item 			keyDefItem	= arguments[1].head();	
	
			if ((keyRef == null) || (keyDefItem == null)) {
				return EmptySequence.getInstance();
			} else {
				final KeyDefInterface 	keyDef 			= getKeyDefFromItem(arguments[1].head());
				final DisplaySuffix		displaySuffix	= keyRef.getDisplaySuffix(keyDef, false);
				final List<Item> 		list 			= new LinkedList<>();
				
				list.add(new StringValue(displaySuffix.keySuffix));
				list.add(new StringValue(displaySuffix.nameSuffix));
	
				return new SequenceExtent(list);
			}

		} catch (MalformedURLException e) {
			logger.error(e, e);
			throw new XPathException("Failed to get display suffix: " + e.getMessage());
		}
	}

}
