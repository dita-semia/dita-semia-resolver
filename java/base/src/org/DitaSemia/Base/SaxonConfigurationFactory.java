package org.DitaSemia.Base;

import java.net.URL;

import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.tree.util.DocumentNumberAllocator;

abstract public class SaxonConfigurationFactory {

	abstract public Configuration createConfiguration();
	

	protected final static NamePool					namePool 				= new NamePool();
	protected final static DocumentNumberAllocator	documentNumberAllocator	= new DocumentNumberAllocator();

	public static Configuration loadConfiguration(URL configUrl) {
		try {
			final Configuration configuration = Configuration.readConfiguration(new SAXSource(new InputSource(configUrl.toExternalForm())));
			
			// make compatible with base configration
			configuration.setNamePool(namePool);
			configuration.setDocumentNumberAllocator(documentNumberAllocator);

			return configuration;
		} catch (Exception e) {
			throw new RuntimeException("failed to load saxon configuration file (" + DocumentCache.decodeUrl(configUrl) + "): " + e.getMessage());
		}
	}

}
