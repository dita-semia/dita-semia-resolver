package org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.KeyDefExtensionFunctionCall;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;

public class GetLocationCall extends KeyDefExtensionFunctionCall {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GetLocationCall.class.getName());


	@Override
	public Sequence call(KeyDefInterface keyDef, XPathContext context, Sequence[] arguments) throws XPathException {
		final URL 				defUrl 	= keyDef.getDefUrl();
		final String 			defId	= keyDef.getDefId();
		if ((defUrl == null) || (defId == null)) {
			return EmptySequence.getInstance();	
		} else {
			try {
				final StringBuffer	location = new StringBuffer();

				final URL 	baseUrl 	= new URL(((AnyURIValue)arguments[1].head()).asString());
				final File 	baseFile	= new File(baseUrl.getFile());
				final File 	destFile	= new File(defUrl.getFile());
				if (!baseFile.equals(destFile)) {
					//logger.info("base: " + baseFile.getParent());
					//logger.info("dest: " + destFile.getPath());
					Path basePath	= Paths.get(baseFile.getParent());
					Path destPath	= Paths.get(destFile.getPath());
					Path relPath	= basePath.relativize(destPath);
					location.append(relPath.toString().replace("\\", "/"));
				}
				location.append(DitaUtil.HREF_URL_ID_DELIMITER);
				final String 	defAncestorTopicId	= keyDef.getDefAncestorTopicId();
				if (defAncestorTopicId != null) {
					location.append(defAncestorTopicId);
					location.append(DitaUtil.HREF_TOPIC_ID_DELIMITER);
				}
				location.append(defId); 
				return new StringValue(location.toString());
			} catch (MalformedURLException e) {
				throw new XPathException(e.getMessage());
			}
		}
	}

}
