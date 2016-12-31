package org.DitaSemia.Base.DocumentCaching;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.DitaSemia.Base.BookCache;
import org.DitaSemia.Base.BookCacheProvider;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceExtent;

public class GetChildTopicsCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(GetChildTopicsCall.class.getName());


	protected final BookCacheProvider bookCacheProvider;

	public GetChildTopicsCall(BookCacheProvider bookCacheProvider) {
		this.bookCacheProvider	= bookCacheProvider;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		
		try {
			final SaxonNodeWrapper 	topicNode	= new SaxonNodeWrapper((NodeInfo)arguments[0].head(), null);
			final BookCache			bookCache	= bookCacheProvider.getBookCache(topicNode.getBaseUrl());
			
			//logger.info("GetChildTopicsCall(" + FileUtil.decodeUrl(topicNode.getBaseUrl()) + ")");
			
			final Collection<FileCache> childTopics = bookCache.getChildTopics(topicNode);
			if (childTopics != null) {
				List<Item> 	list = new LinkedList<>();
				for (FileCache childTopic : childTopics) {
					//logger.info("  - '" + childTopic.getRootNode().getUnderlyingNode().getDisplayName() + "'");
					list.add(childTopic.getRootNode().getUnderlyingNode());
				}
				return new SequenceExtent(list);
			} else {
				//logger.info("  null");
				return EmptySequence.getInstance();
			}
		} catch (XPathException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException("Error: ", e.getMessage());
		}
	}

}
