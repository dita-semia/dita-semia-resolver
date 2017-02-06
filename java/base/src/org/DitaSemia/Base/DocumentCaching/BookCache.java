package org.DitaSemia.Base.DocumentCaching;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.DitaSemia.Base.ConfigurationInitializer;
import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.ProgressListener;
import org.DitaSemia.Base.SaxonDocumentBuilder;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.XPathCache;
import org.DitaSemia.Base.XslTransformerCache;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefListInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyTypeDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyTypeDefListInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyspecInterface;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.AncestorPathDef;
import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Base.XsltConref.XsltConrefCache;
import org.apache.log4j.Logger;

public class BookCache implements KeyDefListInterface, KeyTypeDefListInterface {

	private static final Logger logger = Logger.getLogger(BookCache.class.getName());

	public static final String 	NAMESPACE_URI			= "http://www.dita-semia.org/book-cache";
	public static final String 	NAMESPACE_PREFIX		= "bc";
	
	public static final String	DATA_NAME_TYPE_DEF_URI	= "ikd:TypeDefUri";
	public static final QName	NAME_KEY_TYPE_DEF_LIST	= new QName("KeyTypeDefList");
	public static final QName	NAME_KEY_TYPE_DEF		= new QName(KeyTypeDef.ELEMENT);
	
	public static final String 	ANY_KEY_TYPE			= "*";

	public static final String 	CONFIG_FILE_URL 		= "/cfg/book-cache-saxon-config.xml";
	
	
	private HashMap<String, KeyDefInterface> 	keyDefByRefString 	= new HashMap<>();
	private HashMap<String, KeyDefInterface> 	keyDefByUrlAndId	= new HashMap<>();
	private HashMap<String, KeyTypeDef> 		keyTypeDefByName	= new HashMap<>();
	private HashMap<String, FileCache> 			fileByUrl			= new HashMap<>();
	private HashMap<String, TopicRef> 			topicRefByUrl		= new HashMap<>();
	
	

	private final URL 						rootDocumentUrl;
	private final ConfigurationInitializer	configurationInitializer;
	private final XsltConrefCache 			xsltConrefCache;
	private final URL						ditaOtUrl;
	private final URL						globalKeyTypeDefUrl;
	private final String					hddCachePath;
	//private final String					language;

	private final Configuration				defaultConfiguration;
	private final XslTransformerCache		extractTransformerCache;	// for data extraction from cached document -> attribute defaults are already resolved!
	private final XPathCache 				xPathCache;
	private final SaxonDocumentBuilder		documentBuilder;
	//private final EntityResolver			entityResolver;
	private final boolean					expandAttributeDefaults;
	private final XMLOutputFactory			xmlOutputFactory;

	private final String					appendixPrefix;

	private final Queue<NeedsInit>			initQueue = new PriorityQueue<>(100, new NeedsInit.PriorityComparator());
	private ProgressListener				cacheProgressListener 	= null;
	private int								cachedFileCount			= 0;

	
	public BookCache(
			URL 						rootDocumentUrl,
			ConfigurationInitializer	configurationInitializer,
			XsltConrefCache 			xsltConrefCache,
			SaxonDocumentBuilder		documentBuilder,
			XslTransformerCache			extractTransformerCache,
			boolean						expandAttributeDefaults,
			URL 						ditaOtUrl,
			URL 						globalKeyTypeDefUrl, 
			String						hddCachePath,
			String 						language) {
		
		this.rootDocumentUrl 			= rootDocumentUrl;
		this.configurationInitializer	= configurationInitializer;
		this.xsltConrefCache			= xsltConrefCache;
		this.documentBuilder			= documentBuilder;
		this.extractTransformerCache	= extractTransformerCache;
		this.expandAttributeDefaults	= expandAttributeDefaults;
		this.defaultConfiguration		= createConfiguration(); // needs to be done after this.configurationInitializer has been set
		
		this.ditaOtUrl					= ditaOtUrl;
		this.hddCachePath				= hddCachePath;
		this.globalKeyTypeDefUrl		= globalKeyTypeDefUrl;

		appendixPrefix		= getAppendixPrefix(language);
		xPathCache 			= new XPathCache(defaultConfiguration);
		xmlOutputFactory 	= XMLOutputFactory.newInstance();
		

		if (hddCachePath != null) {
			final File hddCacheFolder = new File(hddCachePath);
			hddCacheFolder.mkdirs();
		}
	}

	public void fillCache(ProgressListener progressListener) {
		cacheProgressListener 	= progressListener;
		try {
			final long startTime = Calendar.getInstance().getTimeInMillis();
			
			if (globalKeyTypeDefUrl != null) {
				parseKeyTypeDefFile(globalKeyTypeDefUrl);
			}
			
			final Source 	source 	= defaultConfiguration.getURIResolver().resolve(rootDocumentUrl.toString(), "");
			initQueue.clear();
			createFileCache(source, null);
			if (!initQueue.isEmpty()) {
				while (!initQueue.isEmpty()) {
					initQueue.poll().init();
				}
				
				final long time = Calendar.getInstance().getTimeInMillis() - startTime;
				logger.info("fillCache done: " + time + "ms, " + fileByUrl.size() + " files, " + keyDefByRefString.size() + " keys (" + FileUtil.decodeUrl(source.getSystemId()) + ")");
			}


			cachedFileCount	= fileByUrl.size();
		} catch (TransformerException e) {
			logger.error(e);
			cachedFileCount	= 0;
		}
		cacheProgressListener = null;
	}
	
	public Configuration createConfiguration() {
		final Configuration configuration = SaxonDocumentBuilder.loadConfiguration(XsltConref.class.getResource(CONFIG_FILE_URL));
		
		configuration.registerExtensionFunction(new AncestorPathDef(this));
		
		if (configurationInitializer != null) {
			configurationInitializer.initConfig(configuration);
		}
		return configuration;
	}
	
	public URL getRootDocumentUrl() {
		return rootDocumentUrl;
	}
	
	public boolean isUrlIncluded(URL url) {
		boolean isIncluded;
		final String urlDecoded = FileUtil.decodeUrl(url);
		if (urlDecoded == null) {
			isIncluded = false;
		} else {
			isIncluded = fileByUrl.containsKey(urlDecoded);
		}
		//logger.info("isUrlIncluded(" + url + ") -> " + isIncluded);
		return isIncluded;
	}


	public FileCache createFileCache(Source source, String topicrefClass) {
		final String 	decodedUrl	= FileUtil.decodeUrl(source.getSystemId());
		FileCache 		fileCache	= null;
		
		//logger.info("Source: " + decodedUrl + ", " + source.getClass());

		if (fileByUrl.containsKey(decodedUrl)) {
			logger.error("ERROR: File included twice: '" + decodedUrl + "' - ignored second one.");
		} else {
			fileCache = new FileCache(source, expandAttributeDefaults, topicrefClass, this);
			fileByUrl.put(decodedUrl, fileCache);
			initQueue.add(fileCache);
		}
		return fileCache;
	}

	public TopicRef createTopicRef(FileCache containingFile, FileCache refFile, TopicRefContainer parentTopicRefContainer, NodeWrapper nodeWrapper) {
		final TopicRef topicRef = new TopicRef(containingFile, refFile, parentTopicRefContainer, nodeWrapper);
		if (refFile != null) {
			topicRefByUrl.put(refFile.getDecodedUrl(), topicRef);
		}
		return topicRef;
	}
	
	public void fullRefresh(ProgressListener progressListener) {
		//logger.info("refresh");
		
		keyDefByRefString.clear();
		keyDefByUrlAndId.clear();
		keyTypeDefByName.clear();
		fileByUrl.clear();
		topicRefByUrl.clear();
		
		fillCache(progressListener);
	}

	public void addKeyDef(KeyDefInterface keyDef) {
		keyDefByRefString.put(keyDef.getRefString(), keyDef);
		final URL 		defUrl 	= keyDef.getDefUrl();
		final String	defId 	= keyDef.getDefId();
		if ((defUrl != null) && (defId != null)) {
			keyDefByUrlAndId.put(FileUtil.decodeUrl(defUrl) + DitaUtil.HREF_URL_ID_DELIMITER + keyDef.getDefId(), keyDef);
		}
	}

	@Override
	public Collection<KeyDefInterface> getKeyDefs() {
		return keyDefByRefString.values();
	}

	@Override
	public KeyDefInterface getExactMatch(KeyspecInterface keyspec) {
		return getExactMatch(keyspec.getRefString());
	}

	@Override
	public KeyDefInterface getExactMatch(String refString) {
		return keyDefByRefString.get(refString);
	}

	@Override
	public XPathCache getXPathCache() {
		return xPathCache;
	}
	
	public XslTransformerCache getExtractTransformerCache() {
		return extractTransformerCache;
	}

	public SaxonDocumentBuilder getDocumentBuilder() {
		return documentBuilder;
	}


	public FileCache getFile(URL url) {
		return fileByUrl.get(FileUtil.decodeUrl(url));
	}

	public URL getDitaOtUrl() {
		return ditaOtUrl;
	}

	@Override
	public KeyDefInterface getAncestorKeyDef(NodeWrapper node, String keyType) {
		//logger.info("getAncestorKeyDef (" + ((node.isSameNode(node.getRootNode())) ? "#document" : node.getName()) + ", " + keyType + ")");
		final NodeWrapper parent = getParentNode(node);
		if (parent != null) {
			final String		id		= parent.getAttribute(DitaUtil.ATTR_ID, null);
			KeyDefInterface 	keyDef 	= null; 
			if ((id != null) && (!id.isEmpty())) {
				final String location = DitaUtil.getNodeLocation(parent.getBaseUrl(), id);
				keyDef = keyDefByUrlAndId.get(location);
				//logger.info("  location: '" + location + "', keyDef: " + keyDef);
				if ((keyDef != null) && 
						(keyType != null) && 
						(!keyType.equals(ANY_KEY_TYPE)) && 
						(!keyType.equals(keyDef.getType()))){
					keyDef = null;	// type is not matching
				}
			}
			if (keyDef != null) {
				//logger.info("  keyDef: " + keyDef);
				return keyDef;
			} else {
				return getAncestorKeyDef(parent, keyType);
			}
		} else {
			//logger.info("  keyDef: " + null);
			return null;
		}
	}
	
	public Collection<FileCache> getChildTopics(NodeWrapper topicNode) {
		//logger.info("getChildTopics(" + topicNode.getName() + ")");
		final String 			decodedUrl 	= FileUtil.decodeUrl(topicNode.getBaseUrl());
		final TopicRef	topicRef	= topicRefByUrl.get(decodedUrl);
		//logger.info("topicRef: " + topicRef);
		if (topicRef != null) {
			return topicRef.getChildTopics();
		} else {
			return null;
		}
	}
	
	public FileCache getRootFile() {
		return fileByUrl.get(FileUtil.decodeUrl(rootDocumentUrl));
	}
	
	public FileCache getParentFile(String decodedUrl) {
		TopicRef		parentTopicRef	= getParentTopicRef(decodedUrl);
		while ((parentTopicRef != null) && (parentTopicRef.getReferencedFile() == null)) {
			parentTopicRef = getParentTopicRef(parentTopicRef);
		}
		if (parentTopicRef != null) {
			return parentTopicRef.getReferencedFile();
		} else {
			return null;
		}
	}
	
	public NodeWrapper getParentNode(NodeWrapper node) {
		NodeWrapper parent = node.getParent();
		if (parent == null) {
			final FileCache	parentFile	= getParentFile(FileUtil.decodeUrl(node.getBaseUrl()));
			if (parentFile != null) {
				parent = parentFile.getRootElement();
			}
		}
		return parent;
	}

	public TopicRef getParentTopicRef(TopicRef topicRef) {
		//logger.info("getParentTopicRef: " + topicRef);
		if (topicRef != null) {
			TopicRefContainer parentContainer = getParentTopicRefContainer(topicRef);
			while ((parentContainer != null) && (!(parentContainer instanceof TopicRef))) { 
				parentContainer = getParentTopicRefContainer(parentContainer);
			}
			if (parentContainer != null) {
				return (TopicRef)parentContainer;
			} else {
				return null;
			}
		} else {
			//logger.info("  null");
			return null;
		}
	}
	
	private TopicRef getParentTopicRef(String decodedUrl) {
		return getParentTopicRef(topicRefByUrl.get(decodedUrl));
	}
	
	private TopicRefContainer getParentTopicRefContainer(TopicRefContainer container) {
		//logger.info("getParentTopicRefContainer: " + container);
		if (container instanceof TopicRef) {
			return ((TopicRef)container).getParentContainer();
		} else if (container instanceof FileCache) {
			return topicRefByUrl.get(((FileCache)container).getDecodedUrl());
		} else {
			return null;
		}
	}
	
	public TopicRef getTopicRef(String decodedUrl) {
		return topicRefByUrl.get(decodedUrl);
	}
	
	public StringBuffer getTopicNum(TopicRef topicRef) {
		//logger.info("getTopicNum: " + topicRef);
		if (topicRef != null) {
			StringBuffer num = null;
			final String localNum = topicRef.getLocalNum();
			final TopicRef parentTopicRef = getParentTopicRef(topicRef);
			if (parentTopicRef != null) {
				num = getTopicNum(parentTopicRef);
				if ((num != null) && (localNum != null)) {
					num.append(DitaUtil.TOPIC_NUM_DELIMITER);
					num.append(topicRef.getLocalNum());
				}
			} else {
				if (localNum != null) {
					num = new StringBuffer();
					num.append(localNum);
				} else {
					// no numbering when the root topic has no number
				}
			}
			return num;
		} else {
			return null;
		}
	}

	public StringBuffer getTopicNum(String decodedUrl) {
		return getTopicNum(topicRefByUrl.get(decodedUrl));	
	}
	
	public void createKeyTypeDef(XdmNode node, KeyTypeDef parent) {
		KeyTypeDef childParent = parent;
		//logger.info("nodeName: " + node.getNodeName());
		if (node.getNodeName().equals(NAME_KEY_TYPE_DEF)) {
			final KeyTypeDef keyTypeDef = KeyTypeDef.fromNode(new SaxonNodeWrapper(node.getUnderlyingNode(), null), parent);
			keyTypeDefByName.put(keyTypeDef.getName(), keyTypeDef);
			childParent = keyTypeDef;
			//logger.info("keyTypeDef: " + keyTypeDef);
		}
		final XdmSequenceIterator 	iterator = node.axisIterator(Axis.CHILD, NAME_KEY_TYPE_DEF);
		while (iterator.hasNext()) {
			createKeyTypeDef((XdmNode)iterator.next(), childParent);
		}
	}

	@Override
	public KeyTypeDef getKeyTypeDef(String typeName) {
		final KeyTypeDef keyTypeDef = keyTypeDefByName.get(typeName);
		return (keyTypeDef != null) ? keyTypeDef : KeyTypeDef.DEFAULT;
	}



	public void parseKeyTypeDefFile(URL url) {
		try {
			final InputStream inputStream = new BufferedInputStream(new FileInputStream(url.getFile()));
			final StreamSource 	source 		= new StreamSource(inputStream);
			source.setSystemId(url.toExternalForm());
			//logger.info("source.getSystemId(): " + source.getSystemId());
			parseKeyTypeDefSource(source);
		} catch (FileNotFoundException e) {
			logger.error(e, e);
		}
	}


	public void parseKeyTypeDefSource(Source source) {
		try {
			final XdmNode 				rootNode = documentBuilder.build(source, false);
			final XdmSequenceIterator 	iterator = rootNode.axisIterator(Axis.CHILD, BookCache.NAME_KEY_TYPE_DEF_LIST);
			
			while (iterator.hasNext()) {
				createKeyTypeDef((XdmNode)iterator.next(), KeyTypeDef.DEFAULT);
			}
		} catch (Exception e) {
			logger.error("Error parsing KeyTypeDef file '" + FileUtil.decodeUrl(source.getSystemId()) + "':");
			logger.error(e, e);
		}
	}
	
	public String getAppendixPrefix() {
		return appendixPrefix;
	}
	
	public static String getAppendixPrefix(String language) {
		if (language == null) {
			return "Appendix ";
		} if (language.equals("de_DE")) {
			return "Anhang ";
		} else {
			return "Appendix ";
		}
	}

	public Collection<KeyDefInterface> getMatchingKeyDefs(Set<String> typeFilter, List<String> namespaceFilter) {
		// TODO improve performance ...
		final Collection<KeyDefInterface> list = new LinkedList<>();
		
		for (KeyDefInterface keyDef : keyDefByRefString.values()) {
			if ((keyDef.matchesTypeFilter(typeFilter)) && (keyDef.matchesNamespaceFilter(namespaceFilter))) {
				list.add(keyDef);
			}
		}

		return list;
	}

	public XsltConrefCache getXsltConrefCache() {
		return xsltConrefCache;
	}

	public String getHddCachePath() {
		return hddCachePath;
	}


	public XMLOutputFactory getXmlOutputFactory() {
		return xmlOutputFactory;
	}

	public NodeWrapper getNodeByLocation(String defLocation) {
		if ((defLocation != null) && (!defLocation.isEmpty()) && (defLocation.contains(DitaUtil.HREF_URL_ID_DELIMITER))) {
			final int 		splitPos	= defLocation.indexOf(DitaUtil.HREF_URL_ID_DELIMITER);
			final String 	url 		= defLocation.substring(0, splitPos);
			final String 	id 			= defLocation.substring(splitPos + 1);
			final FileCache fileCache = fileByUrl.get(url);
			if (fileCache != null) {
				return fileCache.getElementByRefId(id);
			}
		}
		return null;
	}

	public void addNeedsInit(NeedsInit needsInit) {
		initQueue.add(needsInit);
	}

}
