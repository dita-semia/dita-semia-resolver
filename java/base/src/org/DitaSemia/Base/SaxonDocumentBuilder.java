package org.DitaSemia.Base;


import javax.xml.transform.Source;

import org.apache.log4j.Logger;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

public class SaxonDocumentBuilder {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SaxonDocumentBuilder.class.getName());
	
	protected final Configuration 	configuration;
	protected final DocumentBuilder documentBuilder;
	
	public SaxonDocumentBuilder(Configuration configuration) {
		this.configuration = configuration; 
		final Processor processor = new Processor(configuration);
		documentBuilder = processor.newDocumentBuilder();
	}

	public XdmNode build(Source source) throws SaxonApiException{
		return documentBuilder.build(source);
	}
	
}
