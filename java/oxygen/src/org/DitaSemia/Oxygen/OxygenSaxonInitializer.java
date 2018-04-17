package org.DitaSemia.Oxygen;

import javax.xml.transform.TransformerException;

import org.DitaSemia.Base.SaxonDocumentBuilder;
import org.DitaSemia.Base.XsltConref.XsltConrefCache;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Initializer;

public class OxygenSaxonInitializer implements Initializer {

	public static final String 	NAMESPACE_URI		= "http://www.dita-semia.org/oxygen";
	public static final String 	NAMESPACE_PREFIX	= "dso";
	
	@Override
	public void initialize(Configuration configuration) throws TransformerException {
		SaxonDocumentBuilder.makeConfigurationCompatible(configuration);
		
		final BookCacheHandler bookCacheHandler = BookCacheHandler.getInstance();
		XsltConrefCache.registerExtensionFunctions(
				configuration, 
				bookCacheHandler, 
				bookCacheHandler.getXsltConrefCache(), 
				bookCacheHandler.getDocumentBuilder());
	}

}
