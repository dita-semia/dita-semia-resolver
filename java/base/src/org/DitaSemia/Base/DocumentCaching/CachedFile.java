package org.DitaSemia.Base.DocumentCaching;

import java.util.Collection;
import java.util.LinkedList;

import net.sf.saxon.s9api.XdmNode;

import org.DitaSemia.Base.DocumentCache;
import org.DitaSemia.Base.NodeWrapper;
import org.apache.log4j.Logger;

public class CachedFile {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CachedFile.class.getName());
	
	protected final String 		decodedUrl;
	protected final XdmNode		rootNode;
	protected final NodeWrapper	rootWrapper;
	
	protected final Collection<CachedTopicRef> rootTopicRefs = new LinkedList<>();

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
		final String classAttr = rootWrapper.getAttribute(DocumentCache.ATTR_CLASS, null);
		if (classAttr != null) {
			return classAttr.contains(DocumentCache.CLASS_MAP);
		} else {
			return false;
		}
	}
	
	public void addRootTopicRef(CachedTopicRef rootTopicRef) {
		//logger.info("addRootTopicRef : " + rootTopicRef);
		//logger.info("  parent: " + this);
		rootTopicRefs.add(rootTopicRef);
	}

	public Collection<CachedFile> getRootTopics() {
		//logger.info("  getRootTopics: " + toString());
		return CachedTopicRef.getChildTopics(rootTopicRefs);
	}
	
	public String toString() {
		return "CachedFile - url: " + decodedUrl + ", rootNode: " + rootWrapper.getName(); 
	}

}
