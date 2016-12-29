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
import org.DitaSemia.Base.DocumentCache;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.apache.log4j.Logger;

public class FileCache extends TopicRefContainer {

	private static final Logger logger = Logger.getLogger(FileCache.class.getName());
	
	protected static final String	LINK_TITLE_XSL	= "plugins/org.dita-semia.resolver/xsl/cache/link-title.xsl";
	
	protected final String 				decodedUrl;
	protected final XdmNode				rootNode;
	protected final DocumentCache 		documentCache;
	protected final NodeWrapper			rootWrapper;
	
	protected final Collection<KeyDefInterface>		keyDefList 			= new LinkedList<>();
	protected final Map<String, SaxonNodeWrapper>	nodeByRefId			= new HashMap<>();
	protected final Map<String, String>				linkTitleByRefId	= new HashMap<>();
	
	

	public FileCache(String decodedUrl, XdmNode rootNode, DocumentCache documentCache) {
		this.decodedUrl 	= decodedUrl;
		this.rootNode		= rootNode;
		this.documentCache	= documentCache;
		this.rootWrapper	= new SaxonNodeWrapper(rootNode.getUnderlyingNode(), documentCache.getXPathCache());
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
	
	public Collection<KeyDefInterface> getKeyDefList() {
		return keyDefList;
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
		return "FileCache - url: " + decodedUrl + ", rootNode: " + rootWrapper.getName(); 
	}
	
	public void parse() throws TransformerException {
		parseNode(getRootNode(), this, null);
	}
	
	public SaxonNodeWrapper getElementByRefId(String refId) {
		return nodeByRefId.get(refId);
	}
	
	public String getLinkTitle(String refId) {
		String linkTitle = linkTitleByRefId.get(refId);
		if (linkTitle == null) {
			final SaxonNodeWrapper linkedNode = nodeByRefId.get(refId);
			if (linkedNode != null) {
				linkTitle = extractString(linkedNode, LINK_TITLE_XSL);
				//logger.info("linkTitle for '" + refId + "': '" + linkTitle + "'");
				linkTitleByRefId.put(refId, linkTitle);
			}
		}
		return linkTitle;
	}
	
	private String extractString(SaxonNodeWrapper node, String xslUrl) {
		try {
			final XsltExecutable 	executable 		= documentCache.getExtractTransformerCache().getExecutable(new URL(documentCache.getDitaOtUrl(), xslUrl));
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
			
			final SaxonNodeWrapper nodeWrapper = new SaxonNodeWrapper(node.getUnderlyingNode(), documentCache.getXPathCache());
			
			final KeyDef keyDef = KeyDef.fromNode(nodeWrapper);
			if (keyDef != null) {
				keyDefList.add(keyDef);
				documentCache.addKeyDef(keyDef);
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
					refFile = documentCache.createFileCache(nodeWrapper.resolveUri(href));
				}
			}

			topicRef = documentCache.createTopicRef(this, refFile, parentTopicRefContainer, nodeWrapper);
			if (refFile != null) {
				refFile.parse();
			}
		}
		return topicRef;
	}


	private void parseKeyTypeDefNode(NodeWrapper nodeWrapper, String classAttr) throws TransformerException {
		if (classAttr.contains(DitaUtil.CLASS_DATA)) {
			final String nameAttr = nodeWrapper.getAttribute(DitaUtil.ATTR_NAME, null);
			if ((nameAttr != null) && (nameAttr.equals(DocumentCache.DATA_NAME_TYPE_DEF_URI))) {
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
					documentCache.parseKeyTypeDefSource(source);
				}
			}
		}
	}
		
}
