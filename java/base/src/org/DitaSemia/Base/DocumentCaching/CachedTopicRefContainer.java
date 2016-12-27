package org.DitaSemia.Base.DocumentCaching;

import java.util.Collection;
import java.util.LinkedList;

public class CachedTopicRefContainer {
	
	protected final Collection<CachedTopicRef> 		childTopicRefs 	= new LinkedList<>();
	
	protected int currPos 	= 0;
	protected int lastType	= CachedTopicRef.TYPE_UNKNOWN;

	protected int addChildTopicRef(CachedTopicRef childTopicRef) {
		//logger.info("addChildTopicRef : " + childTopicRef);
		//logger.info("  parent: " + this);
		childTopicRefs.add(childTopicRef);
		final int currType = childTopicRef.getType();
		if (currType == lastType) {
			++currPos;
		} else {
			currPos = 1;	// 1st entry of this type
			lastType = currType;
		}
		return currPos;
	}

	public Collection<CachedFile> getChildTopics() {
		final Collection<CachedFile> list = new LinkedList<>();
		for (CachedTopicRef childTopicRef : childTopicRefs) {
			final CachedFile refFile = childTopicRef.getReferencedFile();
			if (refFile.isMap()) {
				list.addAll(refFile.getChildTopics());
			} else {
				//logger.info("   - " + refFile.getDecodedUrl());
				list.add(refFile);
			}
		}
		return list;
	}

}
