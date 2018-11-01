package org.DitaSemia.Base.ExtensionFunctions;

import org.DitaSemia.Base.XslTransformerCacheProvider;

public class ExtractContentTextCall extends XsltExtensionFunctionCall {
	
	public final static String 	SCRIPT_URI 		= "urn:dita-semia:xsl:extract-text-standalone.xsl";
	public final static String 	INITIAL_MODE 	= "ExtractContentText";
		
	public ExtractContentTextCall(XslTransformerCacheProvider xslTransformerCacheProvider) {
		super(xslTransformerCacheProvider, SCRIPT_URI, INITIAL_MODE);
	}
}
