package org.DitaSemia.Base.DocumentCaching;

import java.util.Collection;
import java.util.LinkedList;

public class TopicRefContainer {
	
	protected final Collection<TopicRef> 		childTopicRefs 	= new LinkedList<>();
	protected final boolean						isResourceOnly;
	
	protected int currPos 	= 0;
	protected int lastType	= TopicRef.TYPE_UNKNOWN;
	
	protected TopicRefContainer(boolean isResourceOnly) {
		this.isResourceOnly = isResourceOnly;
	}

	protected int addChildTopicRef(TopicRef childTopicRef) {
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

	public Collection<FileCache> getChildTopics() {
		final Collection<FileCache> list = new LinkedList<>();
		for (TopicRef childTopicRef : childTopicRefs) {
			final FileCache refFile = childTopicRef.getReferencedFile();
			if (refFile == null) {
				// no child topics
			} else if (refFile.isMap()) {
				list.addAll(refFile.getChildTopics());
			} else {
				//logger.info("   - " + refFile.getDecodedUrl());
				list.add(refFile);
			}
		}
		return list;
	}
	
	public boolean isResourceOnly() {
		return isResourceOnly;
	}

}
