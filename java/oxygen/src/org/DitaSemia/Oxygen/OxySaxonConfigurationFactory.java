package org.DitaSemia.Oxygen;

import java.io.StringReader;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXSource;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.util.XMLUtilAccess;
import net.sf.saxon.Configuration;
import net.sf.saxon.jaxp.TransformerImpl;

public class OxySaxonConfigurationFactory {
	
	public static Configuration createConfiguration(boolean saxonEE) {
		try {
			final String 			dummyXsl 	= "<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\"/>";
			final TransformerImpl 	temp 		= (TransformerImpl)PluginWorkspaceProvider.getPluginWorkspace().getXMLUtilAccess().createXSLTTransformer(
					new SAXSource(new org.xml.sax.InputSource(new StringReader(dummyXsl))), 
					null, 
					saxonEE ? XMLUtilAccess.TRANSFORMER_SAXON_ENTERPRISE_EDITION : XMLUtilAccess.TRANSFORMER_SAXON_HOME_EDITION,
					false);
			return temp.getConfiguration();
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException("Failed to create Saxon transformer for oXygen configuration: " + e.getMessage());
		}
	}

	public static void adaptConfiguration(Configuration configuration) {
		/*
		 * To allow the error and output messages to be displayed within oxygen and to use the catalogs the configuration needs to contain the handlers.
		 * To get these create a transformer through oXygen API can take the required handlers from its configuration. 
		 */
		final Configuration baseConfiguration = createConfiguration(false);

		configuration.setLogger(baseConfiguration.getLogger());
		configuration.setModuleURIResolver(baseConfiguration.getModuleURIResolver());
		configuration.setOutputURIResolver(baseConfiguration.getOutputURIResolver());
		configuration.setStandardErrorOutput(baseConfiguration.getStandardErrorOutput());
		configuration.setTraceListener(baseConfiguration.getTraceListener());
		configuration.setURIResolver(baseConfiguration.getURIResolver());
		configuration.setSourceResolver(baseConfiguration.getSourceResolver());
		configuration.setSourceParserClass(baseConfiguration.getSourceParserClass());

		configuration.getDefaultXsltCompilerInfo().setMessageReceiverClassName(baseConfiguration.getDefaultXsltCompilerInfo().getMessageReceiverClassName());
		configuration.getDefaultXsltCompilerInfo().setOutputURIResolver(baseConfiguration.getDefaultXsltCompilerInfo().getOutputURIResolver());
		configuration.getDefaultXsltCompilerInfo().setURIResolver(baseConfiguration.getDefaultXsltCompilerInfo().getURIResolver());
	}

}
