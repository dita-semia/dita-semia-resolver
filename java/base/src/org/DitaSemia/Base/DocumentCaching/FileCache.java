package org.DitaSemia.Base.DocumentCaching;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.apache.log4j.Logger;

public class FileCache extends TopicRefContainer {

	private static final Logger logger = Logger.getLogger(FileCache.class.getName());
	
	public static final String		LINK_TITLE_UNKNOWN		= "???";
	public static final String		LINK_NUM_DELIMITER		= " ";
	private final static String		APPENDIX_NUM_DELIMITER	= ":"; 
	
	protected static final String	LINK_TITLE_XSL		= "plugins/org.dita-semia.resolver/xsl/cache/link-title.xsl";
	protected static final String	LOCAL_TOPIC_NUM_XSL	= "plugins/org.dita-semia.resolver/xsl/cache/local-topic-num.xsl";

	protected final String 				decodedUrl;
	protected final XdmNode				rootXdmNode;
	protected final BookCache 			bookCache;
	protected final SaxonNodeWrapper	rootNode;
	protected final String				topicrefClass;
	
	protected boolean				rootTopicNumInitialized	= false;
	protected String 				rootTopicNum 			= null;
	protected String 				rootTopicNumPrefix 		= null;

	protected final Collection<KeyDefInterface>		keyDefList 			= new LinkedList<>();
	protected final Map<String, SaxonNodeWrapper>	nodeByRefId			= new HashMap<>();
	protected final Map<String, String>				linkTitleByRefId	= new HashMap<>();
	protected final Map<String, String>				linkTextByRefId		= new HashMap<>();
	protected final Map<String, String>				localNumByTopicId	= new HashMap<>();
	protected final Collection<FileCache>			refFileList			= new LinkedList<>();
	
	

	public FileCache(String decodedUrl, XdmNode rootXdmNode, BookCache bookCache, String topicrefClass) {
		this.decodedUrl 	= decodedUrl;
		this.rootXdmNode	= rootXdmNode;
		this.bookCache		= bookCache;
		this.rootNode		= new SaxonNodeWrapper(rootXdmNode.getUnderlyingNode(), bookCache.getXPathCache());
		this.topicrefClass	= topicrefClass;
	}

	public String getDecodedUrl() {
		return decodedUrl;
	}

	public XdmNode getRootXdmNode() {
		return rootXdmNode;
	}

	public NodeWrapper getRootNode() {
		return rootNode;
	}
	
	public Collection<KeyDefInterface> getKeyDefList() {
		return keyDefList;
	}
	
	public boolean isMap() {
		final String classAttr = rootNode.getAttribute(DitaUtil.ATTR_CLASS, null);
		if (classAttr != null) {
			return classAttr.contains(DitaUtil.CLASS_MAP);
		} else {
			return false;
		}
	}
	
	public void mapPosChanged() {
		rootTopicNumInitialized = false;
		for (FileCache refFile : refFileList) {
			refFile.mapPosChanged();
		}
	}

	public String toString() {
		return "FileCache - url: " + decodedUrl + ", rootXdmNode: " + rootNode.getName(); 
	}
	
	public void parse() throws TransformerException {
		parseNode(getRootXdmNode(), this, null);
	}
	
	public SaxonNodeWrapper getElementByRefId(String refId) {
		return nodeByRefId.get(refId);
	}

	/* pass null for refId to get link text to root element */
	public String getLinkText(String refId, NodeWrapper contextNode) {
		String linkText = linkTextByRefId.get(refId);
		if (linkText == null) {
			final SaxonNodeWrapper linkedNode = (refId == null) ? rootNode : nodeByRefId.get(refId);
			//logger.info("getLinkText(" + refId + "): " + linkedNode);
			if (linkedNode != null) {
				linkText = getLinkText(refId, linkedNode);
				//logger.info("linkText for '" + refId + "': '" + linkText + "'");
				linkTextByRefId.put(refId, linkText);
			}
		}
		// TODO: for target in different topic: add link text of target topic. e.g. "Section-Title in x.y TopicTitle" 
		return linkText;
	}
	
	private String getLinkText(String refId, SaxonNodeWrapper linkedNode) {
		String linkText;
		final String classAttr = linkedNode.getAttribute(DitaUtil.ATTR_CLASS, null);
		//logger.info("getLinkText(" + refId + "), class: " + classAttr);
		
		if (classAttr == null) {
			linkText = LINK_TITLE_UNKNOWN;
		} if (classAttr.contains(DitaUtil.CLASS_TOPIC)) {
			final StringBuffer topicNum = getTopicNumPrefix(refId, linkedNode);
			//logger.info("topicNum: " + topicNum);
			if (topicNum != null) {
				topicNum.append(LINK_NUM_DELIMITER);
				topicNum.append(getLinkTitle(refId, linkedNode));
				linkText = topicNum.toString();
			} else {
				linkText = getLinkTitle(refId, linkedNode);
			}
		} else if (classAttr.contains(DitaUtil.CLASS_FIG)) {
			linkText = getLinkTitle(refId, linkedNode);
			// TODO: add actual chapter or appendix number of root and prefix. e.g. "Fig. 1-x: " 
		} else if (classAttr.contains(DitaUtil.CLASS_TABLE)) {
			linkText = getLinkTitle(refId, linkedNode);
			// TODO: add actual chapter or appendix number of root and prefix. e.g. "Tab. 1-x: "
		} else {
			linkText = getLinkTitle(refId, linkedNode);
		}
		//logger.info("  result: " + linkText);
		return linkText;
	}
	
	private String getLinkTitle(String refId, SaxonNodeWrapper linkedNode) {
		String linkTitle = linkTitleByRefId.get(refId);
		if (linkTitle == null) {
			linkTitle = extractString(linkedNode, LINK_TITLE_XSL);
			if ((linkTitle == null) || (linkTitle.isEmpty())) {
				linkTitle = LINK_TITLE_UNKNOWN;
			}
			//logger.info("linkTitle for '" + refId + "': '" + linkTitle + "'");
			linkTitleByRefId.put(refId, linkTitle);
		}
		return linkTitle;
	}

	private void initRootTopicNum() {
		if (!rootTopicNumInitialized) {
			rootTopicNumInitialized = true;
			final StringBuffer rootTopicNumBuf = bookCache.getTopicNum(decodedUrl);
			if (rootTopicNumBuf == null) {
				rootTopicNum 		= null;
				rootTopicNumPrefix 	= null;
			} else {
				rootTopicNum = rootTopicNumBuf.toString();
				if (topicrefClass.contains(DitaUtil.CLASS_APPENDIX)) {
					rootTopicNumPrefix = bookCache.getAppendixPrefix() + rootTopicNum + APPENDIX_NUM_DELIMITER;	
				} else {
					rootTopicNumPrefix = rootTopicNum;
				}
			}
		}
	}
	
	public String getRootTopicNum() {
		initRootTopicNum();
		return rootTopicNum;
	}
	
	public String getRootTopicNumPrefix() {
		initRootTopicNum();
		return rootTopicNumPrefix;
	}
	
	private StringBuffer getTopicNumPrefix(String topicId, SaxonNodeWrapper topicNode) {
		if (topicNode.isSameNode(rootNode)) {
			initRootTopicNum();
			if (rootTopicNumPrefix != null) {
				final StringBuffer topicNumPrefix = new StringBuffer();
				topicNumPrefix.append(rootTopicNumPrefix);
				return topicNumPrefix;
			} else {
				return null;
			}
		} else {
			return getTopicNum(topicId, topicNode);
		}
	}
	
	private StringBuffer getTopicNum(String topicId, SaxonNodeWrapper topicNode) {
		initRootTopicNum();
		if (rootTopicNum != null) {
			final StringBuffer topicNum = new StringBuffer();
			topicNum.append(rootTopicNum);
			final String localNum = getLocalTopicNum(topicId, topicNode);
			if (localNum != null) {
				topicNum.append(localNum);
			}
			return topicNum;
		} else {
			return null;
		}
	}

	private String getLocalTopicNum(String topicId, SaxonNodeWrapper topicNode) {
		String localNum = localNumByTopicId.get(topicId);
		if (localNum == null) {
			localNum = extractString(topicNode, LOCAL_TOPIC_NUM_XSL);
			localNumByTopicId.put(topicId, localNum);
		}
		return localNum;
	}
	
	private String extractString(SaxonNodeWrapper node, String xslUrl) {
		try {
			final XsltExecutable 	executable 		= bookCache.getExtractTransformerCache().getExecutable(new URL(bookCache.getDitaOtUrl(), xslUrl));
			final XsltTransformer 	xsltTransformer = executable.load();
			xsltTransformer.setInitialContextNode(new XdmNode(node.getNodeInfo()));
			
			final XdmDestination destination = new XdmDestination();
			xsltTransformer.setDestination(destination);
			xsltTransformer.transform();
			
			return destination.getXdmNode().getStringValue();
		} catch (Exception e) {
			logger.error(e, e);
			return "<ERR>";
		}
	}
	

	private void parseNode(XdmNode node, TopicRefContainer parentTopicRefContainer, String parentTopicId) throws TransformerException {
		//logger.info("parseNode: " + node.getUnderlyingNode().getDisplayName() + ", " + node.getNodeKind());
		if (node.getNodeKind() == XdmNodeKind.ELEMENT) {
			
			final SaxonNodeWrapper nodeWrapper = new SaxonNodeWrapper(node.getUnderlyingNode(), bookCache.getXPathCache());
			
			final KeyDef keyDef = KeyDef.fromNode(nodeWrapper);
			if (keyDef != null) {
				keyDefList.add(keyDef);
				bookCache.addKeyDef(keyDef);
			}
			
			final String classAttr = nodeWrapper.getAttribute(DitaUtil.ATTR_CLASS, null);

			//logger.info("parsing element: <" + nodeWrapper.getName() + " class=\"" + classAttr + "\">");

			if (classAttr != null) {
				final TopicRef topicRef = parseTopicRef(nodeWrapper, classAttr, parentTopicRefContainer);
				if (topicRef != null) {
					parentTopicRefContainer = topicRef;	// take it as new parent for child nodes.
				}
				parseKeyTypeDefNode(nodeWrapper, classAttr);
				
				final String id = nodeWrapper.getAttribute(DitaUtil.ATTR_ID, null);
				if ((id != null) && (!id.isEmpty())) {
					if (classAttr.contains(DitaUtil.CLASS_TOPIC)) {
						addElementId(nodeWrapper, null, id);
						parentTopicId = id;
					} else {
						addElementId(nodeWrapper, parentTopicId, id);
					}
				}
			}
			
			final XdmSequenceIterator iterator = node.axisIterator(Axis.CHILD);
			while (iterator.hasNext()) {
				parseNode((XdmNode)iterator.next(), parentTopicRefContainer, parentTopicId);
			}
		}
	}
	
	private void addElementId(SaxonNodeWrapper nodeWrapper, String parentTopicId, String id) {
		nodeByRefId.put(DitaUtil.getRefId(parentTopicId, id), nodeWrapper);
	}

	private TopicRef parseTopicRef(NodeWrapper nodeWrapper, String classAttr, TopicRefContainer parentTopicRefContainer) throws TransformerException {
		//logger.info("parseTopicRef");
		TopicRef topicRef = null;
		if (classAttr.contains(DitaUtil.CLASS_TOPIC_REF)) {
			FileCache refFile = null;
			final String processingRoleAttr = nodeWrapper.getAttribute(DitaUtil.ATTR_PROCESSING_ROLE, null);
			if ((processingRoleAttr == null) || (!processingRoleAttr.equals(DitaUtil.ROLE_RESOURCE_ONLY))) {
				final String href = nodeWrapper.getAttribute(DitaUtil.ATTR_HREF, null);
				if ((href != null) && (!href.isEmpty())) {
					refFile = bookCache.createFileCache(nodeWrapper.resolveUri(href), classAttr);
				}
			}

			topicRef = bookCache.createTopicRef(this, refFile, parentTopicRefContainer, nodeWrapper);
			if (refFile != null) {
				refFile.parse();
				refFileList.add(refFile);
			}
		}
		return topicRef;
	}


	private void parseKeyTypeDefNode(NodeWrapper nodeWrapper, String classAttr) throws TransformerException {
		if (classAttr.contains(DitaUtil.CLASS_DATA)) {
			final String nameAttr = nodeWrapper.getAttribute(DitaUtil.ATTR_NAME, null);
			if ((nameAttr != null) && (nameAttr.equals(BookCache.DATA_NAME_TYPE_DEF_URI))) {
				final String urlAttr = nodeWrapper.getAttribute(DitaUtil.ATTR_VALUE, null);
				if ((urlAttr != null) && (!urlAttr.isEmpty())) {
					Source source;
					final String xtrfAttr = nodeWrapper.getAttribute(DitaUtil.ATTR_XTRF, null);
					if ((xtrfAttr != null) && (!xtrfAttr.isEmpty())) {
						// resolve URI when being called from DitaSemiaOtResolver
						source = nodeWrapper.getUriResolver().resolve(urlAttr, xtrfAttr);
					} else {
						source = nodeWrapper.resolveUri(urlAttr);
					}
					bookCache.parseKeyTypeDefSource(source);
				}
			}
		}
	}
		
}
