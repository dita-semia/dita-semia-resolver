package org.DitaSemia.Base.DocumentCaching;

import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.Type;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.FilterProperties;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Base.XsltConref.TempContextException;
import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Base.XsltConref.XsltConrefCache;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class FileCache extends TopicRefContainer implements NeedsInit {
	
	private static final Logger logger = Logger.getLogger(FileCache.class.getName());

	public static final String		FRAMEWORK_ID			= "4";	
	
	public static final String		LINK_TITLE_UNKNOWN		= "???";
	public static final String		LINK_NUM_DELIMITER		= " ";
	private final static String		APPENDIX_NUM_DELIMITER	= ":"; 
	
	protected static final String	LINK_TITLE_XSL			= "plugin:org.dita-semia.resolver:xsl/cache/link-title.xsl";
	protected static final String	LOCAL_TOPIC_NUM_XSL		= "plugin:org.dita-semia.resolver:xsl/cache/local-topic-num.xsl";

	protected static final String	HC_ROOT					= "hdd-cache";
	protected static final String	HC_DEPENDENCY			= "dependency";
	protected static final String	HC_TOPIC				= "topic";

	protected static final String	HC_ATTR_FRAMEWORK_ID	= "framework-id";
	protected static final String	HC_ATTR_SYSTEM_ID		= "system-id";
	protected static final String	HC_ATTR_TIMESTAMP		= "timestamp";
	protected static final String	HC_ATTR_ROOT_DOC		= "root-doc";
	protected static final String	HC_ATTR_ROOT_NAME		= "root-name";
	protected static final String	HC_ATTR_ROOT_CLASS		= "root-class";
	protected static final String	HC_ATTR_ROOT_TITLE		= "root-title";
	
	private static final int 		INIT_PRIORITY_RESOURCE_ONLY_MAP		= 0;
	private static final int 		INIT_PRIORITY_RESOURCE_ONLY_TOPIC	= 1;
	private static final int		INIT_PRIORITY_MAP					= 2;
	private static final int		INIT_PRIORITY_TOPIC					= 3;
	private static final int 		INIT_PRIORITY_OFFSET_XSLT_CONREF 	= 4;

	protected final Source 				source;
	protected final boolean				expandAttributeDefaults;
	protected final String 				decodedUrl;
	protected final BookCache 			bookCache;
	protected final XsltConrefCache 	xsltConrefCache;
	protected final String				topicrefClass;

	protected SaxonNodeWrapper		rootElement				= null;
	protected String				rootClass				= null;
	protected String				rootTitle				= null;
	protected String				rootName				= null;
	protected boolean				rootTopicNumInitialized	= false;
	protected String 				rootTopicNum 			= null;
	protected String 				rootTopicNumPrefix 		= null;
	protected FilterProperties		rootFilterProperties	= null;

	
	protected final Collection<KeyDefInterface>		keyDefList 				= new LinkedList<>();
	protected final Map<String, SaxonNodeWrapper>	nodeByRefId				= new HashMap<>();
	protected final Map<String, String>				linkTitleByRefId		= new HashMap<>();
	protected final Map<String, String>				linkTextByRefId			= new HashMap<>();
	protected final Map<String, String>				localNumByTopicId		= new HashMap<>();
	protected final Map<String, FileCache>			refFileByHref			= new HashMap<>();
	protected final Collection<ContainedXsltConref>	containedXsltConrefs	= new ArrayList<>();
	protected final Map<String, Set<NodeInfo>>		keyRefsByRefString		= new HashMap<>();
	protected final Set<String>						topicIds				= new HashSet<>();

	protected String 	fileTimestamp;
	protected boolean	isFileParsed;
	protected boolean	containsUncachableXsltConref;
	protected boolean	isKeyRegistrationDone;
	protected boolean	isInitFromCache;
	protected boolean	isRefreshing;
	

	public FileCache(Source source, boolean expandAttributeDefaults, String topicrefClass, boolean isResourceOnly, BookCache bookCache) {
		super(isResourceOnly);
		
		//logger.info("FileCache " + source.getSystemId() + ", isResourceOnly: " + isResourceOnly);
		
		this.source						= source;
		this.expandAttributeDefaults	= expandAttributeDefaults;
		this.decodedUrl 				= FileUtil.decodeUrl(source.getSystemId());
		this.bookCache					= bookCache;
		this.xsltConrefCache			= bookCache.getXsltConrefCache();
		//this.rootNode					= new SaxonNodeWrapper(rootXdmNode.getUnderlyingNode(), bookCache.getXPathCache());
		this.topicrefClass				= topicrefClass;
		
		fileTimestamp 					= FileUtil.getLastModifiedAsString(source.getSystemId());
		isFileParsed					= false;
		containsUncachableXsltConref	= false;
		isKeyRegistrationDone			= false;
		isInitFromCache					= false;
		isRefreshing					= false;
	}

	public String getDecodedUrl() {
		return decodedUrl;
	}

	public String getRootName() {
		return rootName;
	}

	public SaxonNodeWrapper getRootElement() {
		ensureFileIsParsed();
		return rootElement;
	}

	public Collection<KeyDefInterface> getKeyDefList() {
		return keyDefList;
	}

	public Collection<String> getTopicIds() {
		return topicIds;
	}

	public Map<String, Set<NodeInfo>> getKeyRefs() {
		return keyRefsByRefString;
	}
	
	@Override
	public int getPriority() {
		if (this.topicrefClass != null) {
			if (isResourceOnly) {
				if (this.topicrefClass.contains(DitaUtil.CLASS_MAP_REF)) {
					return INIT_PRIORITY_RESOURCE_ONLY_MAP;
				} else {
					return INIT_PRIORITY_RESOURCE_ONLY_TOPIC;
				}
			} else {
				if (this.topicrefClass.contains(DitaUtil.CLASS_MAP_REF)) {
					return INIT_PRIORITY_MAP;
				} else {
					return INIT_PRIORITY_TOPIC;
				}
			}
		} else {
			return 0;
		}
	}

	public boolean isMap() {
		if (rootClass != null) {
			return rootClass.contains(DitaUtil.CLASS_MAP);
		} else {
			return false;
		}
	}
	
	public void mapPosChanged() {
		rootTopicNumInitialized = false;
		for (FileCache refFile : refFileByHref.values()) {
			refFile.mapPosChanged();
		}
	}

	@Override
	public String toString() {
		return "FileCache - url: " + decodedUrl + ", rootElement: " + ((rootElement == null) ? null : rootElement.getName()); 
	}

	@Override
	public void init() {
		//logger.info("init: '" + decodedUrl + "'");
		final long startTime = Calendar.getInstance().getTimeInMillis();
		String initType = "";
		
		// try to load from hddCache
		final String fileHddCachePath 	= getFileHddCachePath();
		//logger.info("fileHddCachePath: " + fileHddCachePath);
		if (fileHddCachePath != null) {
			final boolean success = parseHddCache(fileHddCachePath);
			if (!success) {
				parseFile(fileHddCachePath);
				if ((isHddCachable()) && (!isInitFromCache)) {
					initType = ", complete parsing + write HDD cache";
				} else {
					initType = ", complete parsing";
				}
			} else {
				initType = ", from HDD cache";
			}
		} else {
			parseFile(fileHddCachePath);
		}

		 
		//final long time = Calendar.getInstance().getTimeInMillis() - startTime;
		//logger.info("init (" + decodedUrl + ") done: " + time + "ms" + initType);
	}
	
	public boolean isFileParsed() {
		return isFileParsed;
	}

	private boolean parseHddCache(String fileHddCachePath) {
		try {
			
			final InputSource 	inputSource = new InputSource(new FileReader(fileHddCachePath));
			final XMLReader 	xmlReader 	= XMLReaderFactory.createXMLReader();
			
			rootFilterProperties = FilterProperties.createUnrestricted();
			
			xmlReader.setContentHandler(new HddCacheReader(new URL(source.getSystemId())));
			xmlReader.parse(inputSource);
			
		} catch (FileNotFoundException e) {
			return false;	// normal case -> no need to log anything
		} catch (CacheOutOfDate e) {
			//logger.info(e);
			return false;	// normal case -> no need to log anything
		} catch (SAXException | IOException e) {
			logger.error(e, e);
			return false;
		}
		isKeyRegistrationDone 	= true;
		isInitFromCache 		= true;
		return true;
	}

	private void parseFile(String fileHddCachePath) {
		try {
			//logger.info("parseFile: " + decodedUrl + "(isResourceOnly: " + isResourceOnly + ")");
			
			containsUncachableXsltConref	= false;
			isFileParsed 					= true;	// on failure the value remains true since a reparsing makes no sense.
			
			final XdmNode 				rootNode 		= bookCache.getDocumentBuilder().build(source, expandAttributeDefaults, true);
			final XdmSequenceIterator 	iterator 		= rootNode.axisIterator(Axis.CHILD);
			
			//logger.info(SaxonNodeWrapper.serializeNode(rootNode.getUnderlyingNode()));
			
			rootElement = null;
			while ((iterator.hasNext()) && (rootElement == null)) {
				final XdmNode child =(XdmNode)iterator.next();
				//logger.info("  node: " + node.getNodeName() + ", " + node.getNodeKind());
				if (child.getNodeKind() == XdmNodeKind.ELEMENT) {
					rootElement = new SaxonNodeWrapper(child.getUnderlyingNode(), bookCache.getXPathCache());
				}
			}
			
			if (rootElement != null) {
				parseNode(rootElement.getNodeInfo(), this, null);
				
				rootClass 	= rootElement.getAttribute(DitaUtil.ATTR_CLASS, null);
				rootTitle	= extractString(rootElement, LINK_TITLE_XSL);
				rootName	= rootElement.getName();
				
				rootFilterProperties = FilterProperties.getFromNode(rootElement);

				if ((fileHddCachePath != null) && (isHddCachable()) && (!isInitFromCache) && (fileTimestamp != null)) {
					// don't write cache again, when initialization has already been done from cache.
					writeHddCache(fileHddCachePath, fileTimestamp);
				}
				
				isKeyRegistrationDone = true;
			}
			
		} catch(SaxonApiException | TransformerException e) {
			logger.error(e, e);
			rootElement = null;
		}
	}

	/*
	 * Cachable if:
	 * 	- not referencing other files
	 * 	- not containing uncachable XSLT-Conrefs
	 */
	private boolean isHddCachable() {
		return ((refFileByHref.isEmpty()) && (!containsUncachableXsltConref));
	}

	private String getFileHddCachePath() {
		final String cachepath	= bookCache.getHddCachePath();
		if (cachepath != null) {
			final String systemId	= source.getSystemId();
			final String name 		= FilenameUtils.getBaseName(systemId);
			final String hash		= Integer.toString(Math.abs(systemId.hashCode()));
			return FilenameUtils.concat(cachepath, name + "-" + hash + ".xml");
		} else {
			return null;
		}
	}

	private void writeHddCache(String fileHddCachePath, String fileTimestamp) {
		try {
			final XMLOutputFactory 	factory = bookCache.getXmlOutputFactory();
	        final XMLStreamWriter 	writer	= factory.createXMLStreamWriter(new FileWriter(fileHddCachePath));

			writer.writeStartElement(HC_ROOT);
			writer.writeAttribute(HC_ATTR_FRAMEWORK_ID,	FRAMEWORK_ID);
			writer.writeAttribute(HC_ATTR_SYSTEM_ID, 	source.getSystemId());
			writer.writeAttribute(HC_ATTR_TIMESTAMP, 	fileTimestamp);
			writer.writeAttribute(HC_ATTR_ROOT_DOC, 	bookCache.getRootDocumentUrl().getPath());

			writer.writeAttribute(HC_ATTR_ROOT_NAME, 	rootName);
			if (rootClass != null) {
				writer.writeAttribute(HC_ATTR_ROOT_CLASS, 	rootClass);	
			}
			if (rootTitle != null) {
				writer.writeAttribute(HC_ATTR_ROOT_TITLE, 	rootTitle);
			}
			
			rootFilterProperties.writeToHddCache(writer);
			
			for (ContainedXsltConref containedXsltConref : containedXsltConrefs) {
				containedXsltConref.writeDependencyToHddCache(writer);
			}
			
			for (KeyDefInterface keyDef : keyDefList) {
				keyDef.writeToHddCache(writer);
			}
			
			for (String topicId : topicIds) {
				writer.writeStartElement(HC_TOPIC);
				writer.writeAttribute(DitaUtil.ATTR_ID, topicId);	
				writer.writeEndElement();
			}
			
			writer.writeCharacters("\n");
			writer.writeEndElement();
			
			writer.flush();
			writer.close();
			
		} catch (XMLStreamException | IOException e) {
			logger.error(e, e);
		}
	}
	
	public SaxonNodeWrapper getElementByRefId(String refId) {
		ensureFileIsParsed();
		return nodeByRefId.get(refId);
	}

	/* pass null for refId to get link text to root element */
	public String getLinkText(String refId, NodeWrapper contextNode) {
		String linkText = linkTextByRefId.get(refId);
		if (linkText == null) {
			if (refId == null) {
				linkText = getRootLinkText();
				linkTextByRefId.put(null, linkText);
			} else {
				final SaxonNodeWrapper linkedNode = nodeByRefId.get(refId);
				//logger.info("getLinkText(" + refId + "): " + linkedNode);
				if (linkedNode != null) {
					linkText = getLinkText(refId, linkedNode);
					//logger.info("linkText for '" + refId + "': '" + linkText + "'");
					linkTextByRefId.put(refId, linkText);
				}
			}
		}
		// TODO: for target in different topic: add link text of target topic. e.g. "Section-Title in x.y TopicTitle" 
		return linkText;
	}
	
	private String getRootLinkText() {
		if ((rootClass != null) && (rootClass.contains(DitaUtil.CLASS_TOPIC))) {
			initRootTopicNum();
			if (rootTopicNumPrefix != null) {
				final StringBuffer linkText = new StringBuffer();
				linkText.append(rootTopicNumPrefix);
				linkText.append(LINK_NUM_DELIMITER);
				linkText.append(rootTitle);
				return linkText.toString();
			} else {
				return rootTitle;
			}	
		} else {
			return rootTitle;
		}
	}
	
	private String getLinkText(String refId, SaxonNodeWrapper linkedNode) {
		String linkText;
		final String classAttr = linkedNode.getAttribute(DitaUtil.ATTR_CLASS, null);
		//logger.info("getLinkText(" + refId + "), class: " + classAttr);
		
		if (classAttr == null) {
			linkText = LINK_TITLE_UNKNOWN;
		} else if (classAttr.contains(DitaUtil.CLASS_TOPIC)) {
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
		if (refId == null) {
			return rootTitle;
		} else {
			ensureFileIsParsed();
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
	}

	public void ensureFileIsParsed() {
		if (!isFileParsed) {
			parseFile(getFileHddCachePath());
		}
	}
	
	public boolean isUpdated() {
		final String currTimestamp = FileUtil.getLastModifiedAsString(source.getSystemId());
		return ((fileTimestamp != null) && (fileTimestamp.equals(currTimestamp)));
	}
	
	public void refresh() {
		isRefreshing = true;
		
		keyDefList.clear();
		keyRefsByRefString.clear();
		topicIds.clear();
		isKeyRegistrationDone 	= false;
		isFileParsed 			= false;
		isInitFromCache 		= false;
		fileTimestamp 			= FileUtil.getLastModifiedAsString(source.getSystemId());
		
		parseFile(getFileHddCachePath());
		
		// TODO: handle the case when a filereference has been removed

		isRefreshing = false;
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
		if (topicNode.isSameNode(rootElement)) {
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
	
	public StringBuffer getTopicNum(String topicId) {
		ensureFileIsParsed();
		final SaxonNodeWrapper topicNode = nodeByRefId.get(topicId);
		if (topicNode != null) {
			return getTopicNum(topicId, topicNode);
		} else {
			return null;
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
	
	private String extractString(SaxonNodeWrapper node, String xslUri) {
		try {
			final XsltExecutable 	executable 		= bookCache.getExtractTransformerCache().getExecutable(xslUri);
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
	

	private void parseNode(NodeInfo node, TopicRefContainer parentTopicRefContainer, String parentTopicId) throws TransformerException {
		//logger.info("parseNode: " + node.getDisplayName() + ", " + node.getNodeKind());
		//if (node.getNodeKind() == XdmNodeKind.ELEMENT) {
		if (node.getNodeKind() == Type.ELEMENT) {
			
			final SaxonNodeWrapper nodeWrapper = new SaxonNodeWrapper(node, bookCache.getXPathCache());
			
			if (!isKeyRegistrationDone) {
				final KeyDef keyDef = KeyDef.fromNode(nodeWrapper, isResourceOnly, parentTopicId);
				if (keyDef != null) {
					keyDefList.add(keyDef);
					bookCache.addKeyDef(keyDef);
				}
			}
			
			final XsltConref xsltConref = XsltConref.fromNode(nodeWrapper, xsltConrefCache, false);
			if (xsltConref != null) {
				processXsltConref(xsltConref, parentTopicRefContainer, parentTopicId);
			}
			
			final String refAttr = nodeWrapper.getAttribute(KeyRef.ATTR_REF, KeyRef.NAMESPACE_URI);
			if (refAttr != null) {
				Set<NodeInfo> keyRefs = keyRefsByRefString.get(refAttr);
				if (keyRefs == null) {
					keyRefs = new HashSet<NodeInfo>();
					keyRefsByRefString.put(refAttr, keyRefs);
				}
				keyRefs.add(node);
				bookCache.addKeyRef(refAttr, node);
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
						topicIds.add(id);
						bookCache.addTopicId(id, this);
					} else {
						addElementId(nodeWrapper, parentTopicId, id);
					}
				}
			}

			final AxisIterator 	iterator 	= node.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT);
			NodeInfo child = iterator.next();
			while (child != null) {
				parseNode(child, parentTopicRefContainer, parentTopicId);
				child = iterator.next();
			}
		}
	}

	private void processXsltConref(XsltConref xsltConref, TopicRefContainer parentTopicRefContainer, String parentTopicId) throws TransformerException {
		final ContainedXsltConref containedXsltConref = new ContainedXsltConref(xsltConref, parentTopicRefContainer, parentTopicId);
		
		containsUncachableXsltConref |= containedXsltConref.isUncachable();
		containedXsltConrefs.add(containedXsltConref);
		
		final int stage = xsltConref.getStage();
		//logger.info("XSLT-Conref-stage: " + stage);
		if (stage == XsltConref.STAGE_IMMEDIATELY) {
			containedXsltConref.init();
			//logger.info("parsing xslt-conref done");
		} else if (stage >= XsltConref.STAGE_DELAYED) {
			//logger.info("delayed XSLT-Conref added");
			bookCache.addNeedsInit(containedXsltConref);
		} else {
			// no resolving during parsing 
		}
	}

	private void addElementId(SaxonNodeWrapper nodeWrapper, String parentTopicId, String id) {
		nodeByRefId.put(DitaUtil.getRefId(parentTopicId, id), nodeWrapper);
	}

	private TopicRef parseTopicRef(NodeWrapper nodeWrapper, String classAttr, TopicRefContainer parentTopicRefContainer) throws TransformerException {
		//logger.info("parseTopicRef");
		TopicRef topicRef = null;
		if ((classAttr.contains(DitaUtil.CLASS_TOPIC_REF)) || (classAttr.contains(DitaUtil.CLASS_IMPORT))) {
			FileCache refFile = null;
			final String 	processingRoleAttr = nodeWrapper.getAttribute(DitaUtil.ATTR_PROCESSING_ROLE, null);
			final boolean 	fileIsResourceOnly = 
					(classAttr.contains(DitaUtil.CLASS_IMPORT)) || 
					(parentTopicRefContainer.isResourceOnly() || 
					((processingRoleAttr != null) && (processingRoleAttr.equals(DitaUtil.ROLE_RESOURCE_ONLY))));
			final String href = nodeWrapper.getAttribute((classAttr.contains(DitaUtil.CLASS_IMPORT) ? DitaUtil.ATTR_URI : DitaUtil.ATTR_HREF), null);
			if ((href != null) && (!href.isEmpty())) {
				refFile = refFileByHref.get(href);
				if (refFile == null) {
					refFile = bookCache.createFileCache(nodeWrapper.resolveUri(href), classAttr, fileIsResourceOnly);
					if (refFile != null) {
						refFileByHref.put(href, refFile);
					}
				}
			}
	 		topicRef = bookCache.createTopicRef(this, refFile, parentTopicRefContainer, fileIsResourceOnly, nodeWrapper);
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
		
	protected class HddCacheReader extends DefaultHandler {
		
		protected final URL 	defUrl;
		protected 		KeyDef	lastKeydef = null;
		
		public HddCacheReader(URL defUrl) {
			this.defUrl = defUrl;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			switch (localName) {
			case HC_ROOT:
				checkRoot(attributes);
				break;
			case HC_DEPENDENCY:
				checkDependency(attributes);
				break;
			case HC_TOPIC:
				final String id = attributes.getValue(DitaUtil.ATTR_ID);
				topicIds.add(id);
				bookCache.addTopicId(id, FileCache.this);
				break;
			case KeyDef.HC_KEYDEF:
				final KeyDef keyDef = KeyDef.fromHddCache(defUrl, attributes, isResourceOnly);
				lastKeydef = keyDef;
				keyDefList.add(keyDef);
				bookCache.addKeyDef(keyDef);
				break;
			case KeyDef.HC_KEY_FILTER:
				if (lastKeydef != null) {
					lastKeydef.setKeyFilterFromHddCach(attributes);
				} else {
					throw new SAXException("Unexpected element '" + localName + "'!");	
				}
				//final KeyDefInterface keyDef = KeyDef.fromCache
				break;
			default:
				throw new SAXException("Unexpected element '" + localName + "'!");
			}
		}

		private void checkRoot(Attributes attributes) throws SAXException {
			if ((attributes.getLength() < 1) ||  (!attributes.getQName(0).equals(HC_ATTR_FRAMEWORK_ID)) || (!attributes.getValue(0).equals(FRAMEWORK_ID))) {
				throw new CacheOutOfDate("framework-id");
			}
			for (int i = 1; i < attributes.getLength(); ++i) {
				switch (attributes.getQName(i)) {
				case HC_ATTR_SYSTEM_ID:
					//logger.info(HC_ATTR_SYSTEM_ID + ": " + source.getSystemId() + " / " + attributes.getValue(i));
					if (!source.getSystemId().equals(attributes.getValue(i))) {
						throw new CacheOutOfDate("system-id");
					}
					break;
				case HC_ATTR_TIMESTAMP:
					//logger.info(HC_ATTR_TIMESTAMP + ": " + fileTimestamp + " / " + attributes.getValue(i));
					if ((fileTimestamp == null) || (!fileTimestamp.equals(attributes.getValue(i)))) {
						throw new CacheOutOfDate("root-timestamp");
					}
					break;
				case HC_ATTR_ROOT_DOC:
					if (!bookCache.getRootDocumentUrl().getPath().equals(attributes.getValue(i))) {
						throw new CacheOutOfDate("root-doc");
					}
					break;
				case HC_ATTR_ROOT_NAME:
					rootName = attributes.getValue(i);
					break;
				case HC_ATTR_ROOT_CLASS:
					rootClass = attributes.getValue(i);
					break;
				case HC_ATTR_ROOT_TITLE:
					rootTitle = attributes.getValue(i);
					break;
				default:
					if (FilterProperties.isFilterAttribute(attributes.getQName(i))) {
						rootFilterProperties.set(attributes.getQName(i), attributes.getValue(i));
					} else {
						throw new SAXException("Unexpected attribute '" + attributes.getQName(i) + "' on root element.");
					}
				}
			}
			
		}

		private void checkDependency(Attributes attributes) throws SAXException {
			String systemId 	= null;
			String timestamp	= null;
			for (int i = 0; i < attributes.getLength(); ++i) {
				switch (attributes.getQName(i)) {
				case HC_ATTR_SYSTEM_ID:
					systemId = attributes.getValue(i);
					break;
				case HC_ATTR_TIMESTAMP:
					timestamp = attributes.getValue(i);
					break;
				default:
					throw new SAXException("Unexpected attribute '" + attributes.getQName(i) + "' on dependency element.");
				}
			}
			if ((systemId != null) && (timestamp != null)) {
				if (!timestamp.equals(FileUtil.getLastModifiedAsString(systemId))) {
					throw new CacheOutOfDate("dependency: " + systemId);
				}
			} else {
				throw new SAXException("incomplete dependency element.");
			}
		}
	}

	@SuppressWarnings("serial")
	protected class CacheOutOfDate extends SAXException {
		
		protected final String reason;
		
		CacheOutOfDate(String reason) {
			this.reason = reason;
		}
		
		@Override
		public String getMessage() {
			return reason;
		}
	}
	
	protected class ContainedXsltConref implements NeedsInit {
		
		protected final XsltConref 			xsltConref;
		protected final TopicRefContainer 	parentTopicRefContainer;
		protected final String 				parentTopicId;
		protected final int					stage;
		
		protected ContainedXsltConref(XsltConref xsltConref, TopicRefContainer parentTopicRefContainer, String parentTopicId) {
			this.xsltConref 				= xsltConref;
			this.parentTopicRefContainer	= parentTopicRefContainer;
			this.parentTopicId				= parentTopicId;
			this.stage						= xsltConref.getStage();
		}

		public void writeDependencyToHddCache(XMLStreamWriter writer) throws XMLStreamException {
			//logger.info("writeDependencyToHddCache: stage = " + xsltConref.getStage() + ", isSingleSource: " + xsltConref.isSingleSource());
			
			if ((stage == XsltConref.STAGE_IMMEDIATELY) && (xsltConref.isSingleSource())) {
				final String scriptSystemId = xsltConref.getScriptSystemId();
				if (scriptSystemId != null) {
					writer.writeCharacters("\n  ");
					writer.writeStartElement(HC_DEPENDENCY);				
					writer.writeAttribute(HC_ATTR_SYSTEM_ID, scriptSystemId);
					writer.writeAttribute(HC_ATTR_TIMESTAMP, FileUtil.getLastModifiedAsString(scriptSystemId));
					writer.writeEndElement();
				}
				
				final String sourceSystemId = xsltConref.getSourceSystemId();
				if (sourceSystemId != null) {
					writer.writeCharacters("\n  ");
					writer.writeStartElement(HC_DEPENDENCY);
					writer.writeAttribute(HC_ATTR_SYSTEM_ID, sourceSystemId);
					writer.writeAttribute(HC_ATTR_TIMESTAMP, FileUtil.getLastModifiedAsString(sourceSystemId));
					writer.writeEndElement();
				}
			} else {
				// either not relevant for HddCache or not cachable at all
			}
		}

		public boolean isUncachable() {
			if (stage == XsltConref.STAGE_DISPLAY) {
				return false;	//  no effect on cache at all
			} else if (stage == XsltConref.STAGE_IMMEDIATELY) {
				return (!xsltConref.isSingleSource());
			} else {
				return true;	// delayed xslt-conrefs are not cachable since they depend on the map as a whole.
			}
		}

		@Override
		public void init() {
			//logger.info("init XSLT-Conref: " + xsltConref.getScriptName());
			try {
				final NodeInfo resolved = xsltConref.resolveToNode(null);
				//logger.info(SaxonNodeWrapper.serializeNode(resolved));
				parseNode(resolved, parentTopicRefContainer, parentTopicId);
			} catch (TransformerException e) {
				logger.error("Failed to resolve XSLT-Conref: " + e.getMessage());
			} catch (TempContextException e) {
				logger.error(e, e); // should never happen during parsing
			}
		}
		
		@Override
		public int getPriority() {
			return stage + INIT_PRIORITY_OFFSET_XSLT_CONREF;
		}
	}

	public String getFileTimestamp() {
		return fileTimestamp;
	}
	public FilterProperties getRootFilterProperites() {
		return rootFilterProperties;
	}
}
