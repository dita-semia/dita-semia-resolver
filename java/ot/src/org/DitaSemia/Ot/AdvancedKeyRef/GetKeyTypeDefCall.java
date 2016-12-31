package org.DitaSemia.Ot.AdvancedKeyRef;

import java.util.HashMap;

import javax.xml.stream.XMLStreamException;

import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Base.AdvancedKeyref.KeyTypeDef;
import org.DitaSemia.Ot.DitaSemiaOtResolver;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.DocumentInfo;
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

	protected final DitaSemiaOtResolver 		otResolver;
	protected final HashMap<String, Sequence> 	cache;
	
	public GetKeyTypeDefCall(DitaSemiaOtResolver otResolver, HashMap<String, Sequence> cache) {
		this.otResolver	= otResolver;
		this.cache		= cache;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {

		String	keyTypeName = null;
		
		final SaxonNodeWrapper 	node 	= new SaxonNodeWrapper((NodeInfo)arguments[0].head(), otResolver.getXPathCache());
		final KeyRef 			keyRef	= KeyRef.fromNode(node);
		if (keyRef != null) {
			keyTypeName = keyRef.getType();
		} else {
			final String ikdType = node.getAttribute(KeyDef.ATTR_TYPE, KeyDef.NAMESPACE_URI);
			if ((ikdType != null) && (!ikdType.isEmpty())) {
				keyTypeName = ikdType;
			}
		}

		Sequence keyTypeElement = cache.get(keyTypeName);
		if (keyTypeElement == null) {
			keyTypeElement = createKeyTypeElement(context, keyTypeName);
			cache.put(keyTypeName, keyTypeElement);
		}

		return keyTypeElement;
	}

	private  Sequence createKeyTypeElement(XPathContext context, String keyTypeName) throws XPathException {

		final KeyTypeDef		keyTypeDef	= otResolver.getBookCache().getKeyTypeDef(keyTypeName);
		final Processor 		processor 	= new Processor(context.getConfiguration());
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
