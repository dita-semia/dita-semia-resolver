package org.DitaSemia.Ot.AdvancedKeyRef;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.DitaSemia.Base.FilterAttrSet;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Ot.DitaSemiaOtResolver;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.BuildingStreamWriterImpl;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;

public class GetKeyFilterAttrCall extends ExtensionFunctionCall {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GetKeyFilterAttrCall.class.getName());

	protected final DitaSemiaOtResolver otResolver;

	public GetKeyFilterAttrCall(DitaSemiaOtResolver otResolver) {
		this.otResolver	= otResolver;
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		final Item keyDefItem = arguments[0].head();
		if (keyDefItem != null) {
			final KeyDefInterface keyDef = DitaSemiaOtResolver.getKeyDefFromItem(keyDefItem);
			//logger.info(keyDef + ": " + keyDef.isFilteredKey());
			final FilterAttrSet 			set = keyDef.getKeyFilterAttrSet();
			final Map<String, Set<String>>	map = (set == null) ? null : set.getMap();
			
			if (map != null) {
				final Processor 				processor 	= new Processor(context.getConfiguration());
				final DocumentBuilder 			builder 	= processor.newDocumentBuilder();
				
				try {
					BuildingStreamWriterImpl 	writer 		= builder.newBuildingStreamWriter();
					writer.writeStartElement("Container");
					for (Entry<String, Set<String>> entry : map.entrySet()) {
						writer.writeAttribute(entry.getKey(), String.join(" ", entry.getValue()));
					}
					writer.writeEndElement();
					
					XdmSequenceIterator iterator = writer.getDocumentNode().axisIterator(Axis.CHILD);
					return ((XdmNode)iterator.next()).getUnderlyingNode();
					
				} catch (SaxonApiException | XMLStreamException e) {
					throw new XPathException("Failed to create result-node", e);
				}
			}		
		}
		return EmptySequence.getInstance();
	}

}
