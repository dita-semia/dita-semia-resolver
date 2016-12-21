package org.DitaSemia.Base.DocumentCaching;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;

public class CachedTopicRef {

	private static final Logger logger = Logger.getLogger(CachedTopicRef.class.getName());

	protected final CachedFile 		containingFile;
	protected final CachedFile 		referencedFile;
	protected final CachedTopicRef 	parentTopicRef;
	
	protected final Collection<CachedTopicRef> childTopicRefs = new LinkedList<>();

	public CachedTopicRef(CachedFile containingFile, CachedFile referencedFile, CachedTopicRef parentTopicRef) {
		this.containingFile	= containingFile;
		this.referencedFile = referencedFile;
		this.parentTopicRef	= parentTopicRef;
		
		if (parentTopicRef != null) {
			parentTopicRef.addChildTopicRef(this);
		} else {
			containingFile.addRootTopicRef(this);
		}
	}
	
	private void addChildTopicRef(CachedTopicRef childTopicRef) {
		//logger.info("addChildTopicRef : " + childTopicRef);
		//logger.info("  parent: " + this);
		childTopicRefs.add(childTopicRef);
	}

	public CachedFile getContainingFile() {
		return containingFile;
	}
	
	public CachedFile getReferencedFile() {
		return referencedFile;
	}
	
	public CachedTopicRef getParentTopicRef() {
		return parentTopicRef;
	}

	public Collection<CachedFile> getChildTopics() {
		//logger.info("  getChildTopics: " + toString());
		return getChildTopics(childTopicRefs);
	}
	
	public static Collection<CachedFile> getChildTopics(Collection<CachedTopicRef> topicRefs) {
		final Collection<CachedFile> list = new LinkedList<>();
		for (CachedTopicRef childTopicRef : topicRefs) {
			final CachedFile refFile = childTopicRef.getReferencedFile();
			if (refFile.isMap()) {
				list.addAll(refFile.getRootTopics());
			} else {
				//logger.info("   - " + refFile.getDecodedUrl());
				list.add(refFile);
			}
		}
		return list;
	}
	
	public String toString() {
		return "CachedTopicRef - ref: " + referencedFile.getDecodedUrl() + ", containing: " + containingFile.decodedUrl;
	}

}
