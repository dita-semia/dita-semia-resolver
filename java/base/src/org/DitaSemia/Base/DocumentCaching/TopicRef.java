package org.DitaSemia.Base.DocumentCaching;


import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.NodeWrapper;
import org.apache.log4j.Logger;

public class TopicRef extends TopicRefContainer {
	
	public static final int TYPE_UNKNOWN		= -1;
	public static final int TYPE_FRONTMATTER	= 0;
	public static final int TYPE_CHAPTER		= 1;
	public static final int TYPE_APPENDIX		= 2;
	public static final int TYPE_BACKMATTER		= 3;
	public static final int TYPE_TOPIC			= 4;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TopicRef.class.getName());

	protected final FileCache 				containingFile;
	protected final FileCache 				referencedFile;
	protected final TopicRefContainer parentContainer;
	protected final NodeWrapper 			node;
	protected final int						type;
	protected final int						position;
	protected final String					localNum;

	public TopicRef(FileCache containingFile, FileCache referencedFile, TopicRefContainer parentContainer, NodeWrapper node) {
		this.containingFile		= containingFile;
		this.referencedFile 	= referencedFile;
		this.parentContainer	= parentContainer;
		this.node				= node;
		this.type				= getType(node.getAttribute(DitaUtil.ATTR_CLASS, null));
		this.position			= parentContainer.addChildTopicRef(this);
		this.localNum			= getLocalNum(type, position);
	}
	
	public FileCache getContainingFile() {
		return containingFile;
	}
	
	public FileCache getReferencedFile() {
		return referencedFile;
	}

	public TopicRefContainer getParentContainer() {
		return parentContainer;
	}

	public NodeWrapper getNode() {
		return node;
	}
	
	public String toString() {
		return "TopicRef - ref: " + ((referencedFile == null) ? "null" : referencedFile.getDecodedUrl()) + ", containing: " + containingFile.decodedUrl;
	}
	
	public int getType() {
		return type;
	}
	
	public int getPosition() {
		return position;
	}
	
	public String getLocalNum() {
		return localNum;
	}

	private static String getLocalNum(int type, int position) {
		if ((type == TYPE_FRONTMATTER) || (type == TYPE_FRONTMATTER)) {
			return null;	// no numbering!
		} else if (type == TYPE_APPENDIX) {
			return DitaUtil.numToLetter(position);
		} else if ((type == TYPE_TOPIC) || (type == TYPE_CHAPTER)) {
			return Integer.toString(position);
		} else {
			return null;
		}
	}
	
	private static int getType(String classAttr) {
		if (classAttr.contains(DitaUtil.CLASS_FRONTMATTER)) {
			return TYPE_FRONTMATTER;
		} else if (classAttr.contains(DitaUtil.CLASS_CHAPTER)) {
			return TYPE_CHAPTER;
		} else if (classAttr.contains(DitaUtil.CLASS_APPENDIX)) {
			return TYPE_APPENDIX;
		} else if (classAttr.contains(DitaUtil.CLASS_BACKMATTER)) {
			return TYPE_BACKMATTER;
		} else if (classAttr.contains(DitaUtil.CLASS_TOPIC_REF)) {
			return TYPE_TOPIC;
		} else {
			return TYPE_UNKNOWN;
		}
	}

}
