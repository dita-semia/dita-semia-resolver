package org.DitaSemia.Ot.AdvancedKeyRef;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Ot.DitaSemiaOtResolver;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;

public class GetKeyDefLocationCall extends ExtensionFunctionCall {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GetKeyDefLocationCall.class.getName());

	protected final DitaSemiaOtResolver otResolver;

	public GetKeyDefLocationCall(DitaSemiaOtResolver otResolver) {
		this.otResolver	= otResolver;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		
		final Item contextItem = context.getContextItem();
		if (!(contextItem instanceof NodeInfo)) {
			throw new XPathException("Context item '" + contextItem.getClass() + "' needs to be an instance of NodeInfo.");
		}
		final NodeInfo contextNode = (NodeInfo)contextItem;
		
		final KeyDefInterface keyDef = DitaSemiaOtResolver.getKeyDefFromItem(arguments[0].head());
		final URL 		defUrl 	= keyDef.getDefUrl();
		final String 	defId	= keyDef.getDefId();
		if ((defUrl == null) || (defId == null)) {
			return EmptySequence.getInstance();	
		} else {
			try {
				final URI 		baseUri		= new URI(contextNode.getBaseURI());
				final URI 		relativeUri	= baseUri.relativize(defUrl.toURI());
				final StringBuffer	location = new StringBuffer();
				location.append(relativeUri.getPath());
				location.append(DitaUtil.HREF_URL_ID_DELIMITER);
				final String 	defAncestorTopicId	= keyDef.getDefAncestorTopicId();
				if (defAncestorTopicId != null) {
					location.append(defAncestorTopicId);
					location.append(DitaUtil.HREF_TOPIC_ID_DELIMITER);
				}
				location.append(defId); 
				return new StringValue(location.toString());
			} catch (URISyntaxException e) {
				throw new XPathException(e.getMessage());
			}
		}
	}

}
