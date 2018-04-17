package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;

import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Base.AdvancedKeyref.KeyTypeDef;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.apache.log4j.Logger;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.BuildingStreamWriterImpl;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.trans.XPathException;

public class GetKeyTypeDefCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(GetKeyTypeDefCall.class.getName());

	protected final BookCacheProvider 			bookCacheProvider;
	protected final HashMap<String, Sequence> 	cache;
	
	public GetKeyTypeDefCall(BookCacheProvider bookCacheProvider, HashMap<String, Sequence> cache) {
		this.bookCacheProvider	= bookCacheProvider;
		this.cache				= cache;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		try {
			final Sequence 	node	= arguments[0].head();
			
			if (!(node instanceof NodeInfo)) {
				throw new XPathException("Supplied parameter '" + node + "' is no compatible node.");
			}
			
			final NodeInfo 		nodeInfo 	= (NodeInfo)node;
			final BookCache		bookCache	= bookCacheProvider.getBookCache(new URL(nodeInfo.getBaseURI()));
			final NodeWrapper 	nodeWrapper = new SaxonNodeWrapper(nodeInfo, bookCache.getXPathCache());
			final KeyRef 		keyRef		= KeyRef.fromNode(nodeWrapper);
	
			String	keyTypeName = null;
			if (keyRef != null) {
				keyTypeName = keyRef.getType();
			} else {
				final String ikdType = nodeWrapper.getAttribute(KeyDef.ATTR_TYPE, KeyDef.NAMESPACE_URI);
				if ((ikdType != null) && (!ikdType.isEmpty())) {
					keyTypeName = ikdType;
				}
			}
	
			Sequence keyTypeElement = cache.get(keyTypeName);
			if (keyTypeElement == null) {
				keyTypeElement = createKeyTypeElement(keyTypeName, bookCache, context.getConfiguration());
				cache.put(keyTypeName, keyTypeElement);
			}
	
			return keyTypeElement;
			
		} catch (MalformedURLException e) {
			logger.error(e, e);
			throw new XPathException("Failed to get key type definition: " + e.getMessage());
		}
	}

	private  Sequence createKeyTypeElement(String keyTypeName, BookCache bookCache, Configuration configuration) throws XPathException {

		final KeyTypeDef		keyTypeDef	= bookCache.getKeyTypeDef(keyTypeName);
		final Processor 		processor 	= new Processor(configuration);
		final DocumentBuilder 	builder 	= processor.newDocumentBuilder();
		
		try {
			BuildingStreamWriterImpl 	writer 		= builder.newBuildingStreamWriter();
			
			writer.writeStartElement(KeyTypeDef.ELEMENT);
			writer.writeAttribute(KeyTypeDef.ATTR_NAME, 			keyTypeDef.getName());
			writer.writeAttribute(KeyTypeDef.ATTR_IS_CODE_FONT, 	Boolean.toString(keyTypeDef.isCodeFont()));
			writer.writeAttribute(KeyTypeDef.ATTR_IS_ITALIC_FONT, 	Boolean.toString(keyTypeDef.isItalicFont()));
			writer.writeAttribute(KeyTypeDef.ATTR_PREFIX, 			keyTypeDef.getPrefix());
			writer.writeAttribute(KeyTypeDef.ATTR_SUFFIX, 			keyTypeDef.getSuffix());
			writer.writeAttribute(KeyTypeDef.ATTR_SELECT_PRIORITY, 	Integer.toString(keyTypeDef.getSelectPriority()));
			writer.writeEndElement();
			
			final XdmSequenceIterator iterator = writer.getDocumentNode().axisIterator(Axis.CHILD);
			return iterator.next().getUnderlyingValue();
			
		} catch (SaxonApiException | XMLStreamException e) {
			logger.error(e, e);
			throw new XPathException("Failed to create element: " + e.getMessage());
		}
	}

}
