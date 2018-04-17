package org.DitaSemia.Oxygen.DynamicXmlDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedHashMap;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.sax.SAXSource;

import org.DitaSemia.Base.XslTransformerCache;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Oxygen.BookCacheHandler;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Serializer.Property;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.StringValue;

public class CreateXsd {
	private static final Logger logger = Logger.getLogger(CreateXsd.class.getName());
	
	public static final String PROP_ROOT_TYPE	= "rootType";
	public static final String PROP_ROOT_NAME	= "rootName";

	protected static final String	INPUT_URI	= "urn:dxd-dummy";
	protected static final String	XSL_URI		= "plugin:org.dita-semia.resolver:xsl/dxd/create-xsd.xsl";

	public static final QName PARAM_ROOT_TYPE	= new QName(KeyDefInterface.DXD_NAMESPACE_PREFIX, KeyDefInterface.DXD_NAMESPACE_URI, "rootType");
	public static final QName PARAM_ROOT_NAME	= new QName(KeyDefInterface.DXD_NAMESPACE_PREFIX, KeyDefInterface.DXD_NAMESPACE_URI, "rootName");
	public static final QName PARAM_MAP_URL		= new QName(KeyDefInterface.DXD_NAMESPACE_PREFIX, KeyDefInterface.DXD_NAMESPACE_URI, "mapUrl");
	
	private static final BookCacheHandler bookCacheHandler = BookCacheHandler.getInstance();
	
	public void convert(
					String 							systemID, 
					String 							originalSourceSystemID,
					InputStream 					is, 
					OutputStream 					os, 
					LinkedHashMap<String, String> 	properties)
			 throws IOException {
		
		//logger.info("convert");
		
		final String rootType	= properties.get(PROP_ROOT_TYPE);
		final String rootName	= properties.get(PROP_ROOT_NAME);
		
		//logger.info("systemID: " + systemID);
		//logger.info("originalSourceSystemID: " + originalSourceSystemID);

		final URL		mapUrl		= bookCacheHandler.getCurrMapUrl();
		final BookCache	bookCache 	= bookCacheHandler.getBookCache(mapUrl); 

		try {
			
			final XslTransformerCache	xslTransformerCache	= bookCache.getXsltConrefCache().getTransformerCache();
			final XsltExecutable 		executable 			= xslTransformerCache.getExecutable(XSL_URI);
			final XsltTransformer 		xslTransformer 		= executable.load();
			
			
			final Processor 		processor 	= new Processor(xslTransformerCache.getConfiguration());
			final DocumentBuilder 	builder 	= processor.newDocumentBuilder();
			final XMLReader 		xmlReader 	= XMLReaderFactory.createXMLReader();
			final SAXSource 		source 		= new SAXSource(xmlReader, new InputSource(is));
			final XdmNode 			context 	= builder.build(source);
			xslTransformer.setInitialContextNode(context);
			
			
			final Serializer 	serializer = processor.newSerializer();
			serializer.setOutputProperty(Property.INDENT, 				"yes");
			serializer.setOutputProperty(Property.SAXON_LINE_LENGTH, 	"1000");	// avoid line breaks
			serializer.setOutputStream(os);
			xslTransformer.setDestination(serializer);


			xslTransformer.setParameter(PARAM_ROOT_TYPE, 	XdmValue.wrap(new StringValue(rootType)));
			xslTransformer.setParameter(PARAM_ROOT_NAME, 	XdmValue.wrap(new StringValue(rootName)));
			if (mapUrl != null) {
				xslTransformer.setParameter(PARAM_MAP_URL, 		XdmValue.wrap(new AnyURIValue(mapUrl.toString())));
			}

			xslTransformer.transform();
			
		} catch (Exception e) {
			logger.error(e, e);
			throw new IOException(e.getMessage(), e);
		}
		
	}
}
