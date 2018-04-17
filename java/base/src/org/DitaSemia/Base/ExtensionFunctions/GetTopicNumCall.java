package org.DitaSemia.Base.ExtensionFunctions;

import java.net.URL;

import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;

public class GetTopicNumCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(GetTopicNumCall.class.getName());


	protected final BookCacheProvider 	bookCacheProvider;

	public GetTopicNumCall(BookCacheProvider bookCacheProvider) {
		this.bookCacheProvider	= bookCacheProvider;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		//logger.info("call");
		try {
			final String 	id 	= ((StringValue)arguments[0].head()).getStringValue();
			final URL		url	= new URL(((AnyURIValue)arguments[1].head()).getStringValue());
			
			//logger.info(" refString: " + refString);
			//logger.info(" refUrl: " + refUrl);
			
			final BookCache	bookCache	= bookCacheProvider.getBookCache(url);
			final String 	topicNum	= bookCache.getTopicNum(FileUtil.decodeUrl(url), id);
			
			if (topicNum != null) {
				return new StringValue(topicNum);
			} else {
				//logger.info("done: empty");
				return EmptySequence.getInstance();
			}
		} catch (XPathException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException(e.getMessage());
		}
	}

}
