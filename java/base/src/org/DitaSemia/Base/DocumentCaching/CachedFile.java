package org.DitaSemia.Base.DocumentCaching;

import net.sf.saxon.s9api.XdmNode;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.DocumentCache;
import org.DitaSemia.Base.NodeWrapper;
import org.apache.log4j.Logger;

public class CachedFile extends CachedTopicRefContainer {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CachedFile.class.getName());
	
	protected final String 			decodedUrl;
	protected final XdmNode			rootNode;
	protected final NodeWrapper		rootWrapper;

	public CachedFile(String decodedUrl, XdmNode rootNode, NodeWrapper rootWrapper) {
		this.decodedUrl 	= decodedUrl;
		this.rootNode		= rootNode;
		this.rootWrapper	= rootWrapper; 
	}

	public String getDecodedUrl() {
		return decodedUrl;
	}

	public XdmNode getRootNode() {
		return rootNode;
	}

	public NodeWrapper getRootWrapper() {
		return rootWrapper;
	}
	
	public boolean isMap() {
		final String classAttr = rootWrapper.getAttribute(DitaUtil.ATTR_CLASS, null);
		if (classAttr != null) {
			return classAttr.contains(DitaUtil.CLASS_MAP);
		} else {
			return false;
		}
	}
	
	public String toString() {
		return "CachedFile - url: " + decodedUrl + ", rootNode: " + rootWrapper.getName(); 
	}

}
