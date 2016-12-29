package org.DitaSemia.Base;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;

import org.apache.log4j.Logger;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

public class SaxonCachedDocumentBuilder extends SaxonDocumentBuilder {

	private static final Logger logger = Logger.getLogger(SaxonCachedDocumentBuilder.class.getName());

	protected final static Pattern						schemaPattern		= Pattern.compile("xsi:noNamespaceSchemaLocation=\"([^\"]+)\"");
	
	protected final SaxonConfigurationFactory 			configFactory;
	protected final URIResolver 						uriResolver;
	protected final HashMap<String, DocumentBuilder> 	documentBuilderMap 	= new HashMap<>();
	
	public SaxonCachedDocumentBuilder(SaxonConfigurationFactory configFactory) {
		super(configFactory.createConfiguration());
		
		this.configFactory 	= configFactory;
		this.uriResolver	= configuration.getURIResolver();
	}

	@Override
	public XdmNode build(Source source) throws SaxonApiException {
		final String 	schemaName 		= getSchemaName(source, uriResolver);
		DocumentBuilder documentBuilder	= null;
		if ((schemaName != null) && (documentBuilderMap.containsKey(schemaName))) {
			documentBuilder = documentBuilderMap.get(schemaName);
		} else {
			final Processor processor = new Processor(configFactory.createConfiguration());
			documentBuilder = processor.newDocumentBuilder();
			if (schemaName != null) {
				documentBuilderMap.put(schemaName, documentBuilder);
			}
		}
		//logger.info("build document: '" + DocumentCache.decodeUrl(source.getSystemId())+ "'");
		return documentBuilder.build(source);
	}
	
	
	public static String getSchemaName(Source source, URIResolver uriResolver) {
		String noNamespaceSchemaLocation = null;
		if (source.getSystemId() != null) {
			try {
				final URL 				decodedUrl	= new URL(FileUtil.decodeUrl(source.getSystemId()));
				final BufferedReader 	reader 		= new BufferedReader(new FileReader(decodedUrl.getFile()));
				int i = 0;
				String 	line 	= reader.readLine();
				while ((noNamespaceSchemaLocation == null) && (i++ < 10) && (line != null)) {
					Matcher matcher = schemaPattern.matcher(line);
					if (matcher.find()) {
						noNamespaceSchemaLocation = matcher.group(1);
					}
					line = reader.readLine();
				}
		    	reader.close();
		    	if ((noNamespaceSchemaLocation != null) && (uriResolver != null)) {
		    		final Source xsdSource = uriResolver.resolve(noNamespaceSchemaLocation, source.getSystemId());
		    		//logger.info(noNamespaceSchemaLocation + " -> " + xsdSource.getSystemId());
		    		noNamespaceSchemaLocation = xsdSource.getSystemId();
		    	}
			} catch (Exception e) {
				logger.error(e, e);
			}
		}
    	return noNamespaceSchemaLocation;
	}


	public void clearCache() {
		documentBuilderMap.clear();
	}
}
