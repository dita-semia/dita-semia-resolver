package org.DitaSemia.Base.ExtensionFunctions;

import org.apache.fop.hyphenation.Hyphenation;
import org.apache.fop.hyphenation.Hyphenator;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public class HyphenateWordCall extends ExtensionFunctionCall {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(HyphenateWordCall.class.getName());


	private static final int 		HYPHN_LEFT_MIN	= 2;
	private static final int 		HYPHN_RIGHT_MIN	= 2;
	
	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {

		final String 	word 		= arguments[0].head().getStringValue();
		final String 	delimiter	= arguments[1].head().getStringValue();
		final String 	lang		= arguments[2].head().getStringValue();
		final String 	country		= (arguments[3].head() != null) ? arguments[3].head().getStringValue() : null;

		final Hyphenation 	hyphenation = Hyphenator.hyphenate(lang, country, null, word, HYPHN_LEFT_MIN, HYPHN_RIGHT_MIN);
		//logger.info("hyphenation: '" + hyphenation + "'");
		if (hyphenation != null) {
			int[]				hyphPosList	= hyphenation.getHyphenationPoints();
			int 				prevHyphPos	= 0;
			StringBuffer 		buffer 		= new StringBuffer();
			for (int i = 0; i < hyphenation.length(); ++i) {
				final int hyphPos = hyphPosList[i];
				if (i > 0) {
					buffer.append(delimiter);
				}
				//logger.info("hyphPos: " + hyphPos + ", text: '" + word.substring(prevHyphPos, hyphPos) + "'");
				buffer.append(word.substring(prevHyphPos, hyphPos));
				prevHyphPos = hyphPos;
			}
			buffer.append(word.substring(prevHyphPos));
			//logger.info("buffer: '" + buffer.toString().replace(delimiter, "-") + "'");
			
			return new StringValue(buffer.toString());
		} else {
			return new StringValue(word);
		}
	}

}
