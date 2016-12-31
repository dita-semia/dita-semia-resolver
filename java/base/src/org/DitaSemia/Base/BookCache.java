package org.DitaSemia.Base;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefListInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyTypeDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyTypeDefListInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyspecInterface;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.AncestorPathDef;
import org.DitaSemia.Base.DocumentCaching.FileCache;
import org.DitaSemia.Base.DocumentCaching.TopicRef;
import org.DitaSemia.Base.DocumentCaching.TopicRefContainer;
import org.DitaSemia.Base.XsltConref.XsltConref;
import org.apache.log4j.Logger;

public class BookCache extends SaxonConfigurationFactory implements KeyDefListInterface, KeyTypeDefListInterface {

	private static final Logger logger = Logger.getLogger(BookCache.class.getName());

	public static final String 	NAMESPACE_URI			= "http://www.dita-semia.org/book-cache";
	public static final String 	NAMESPACE_PREFIX		= "bc";
	
	public static final String	DATA_NAME_TYPE_DEF_URI	= "ikd:TypeDefUri";
	public static final QName	NAME_KEY_TYPE_DEF_LIST	= new QName("KeyTypeDefList");
	public static final QName	NAME_KEY_TYPE_DEF		= new QName(KeyTypeDef.ELEMENT);
	
	public static final String 	ANY_KEY_TYPE			= "*";

	public static final String 	CONFIG_FILE_URL 		= "/cfg/book-cache-saxon-config.xml";
	
	private final URL 				rootDocumentUrl;
	private final XPathCache 		xPathCache;
	
	private HashMap<String, KeyDefInterface> 	keyDefByRefString 	= new HashMap<>();
	private HashMap<String, KeyDefInterface> 	keyDefByLocation	= new HashMap<>();
	private HashMap<String, KeyTypeDef> 		keyTypeDefByName	= new HashMap<>();
	private HashMap<String, FileCache> 			fileByUrl			= new HashMap<>();
	private HashMap<String, TopicRef> 			topicRefByUrl		= new HashMap<>();
	
	
	private final SaxonConfigurationFactory configurationFactory;
	private final SaxonDocumentBuilder		documentBuilder;
	private final Configuration				defaultConfiguration;
	private final BookCacheInitializer		initializer;
	private final XslTransformerCache		extractTransformerCache;	// for data extraction from cached document -> attribute defaults are already resolved!
	private final URL						ditaOtUrl;
	
	private ProgressListener				cacheProgressListener 	= null;
	private int								cachedFileCount			= 0;

		
	/* used by oXygen */
	public BookCache(URL rootDocumentUrl, BookCacheInitializer initializer, SaxonConfigurationFactory configurationFactory, URL ditaOtUrl) {
		this.rootDocumentUrl 			= rootDocumentUrl;
		this.initializer				= initializer;
		this.configurationFactory		= configurationFactory;
		this.defaultConfiguration		= configurationFactory.createConfiguration();
		this.documentBuilder			= new SaxonCachedDocumentBuilder(this);
		this.ditaOtUrl				= ditaOtUrl;

		registerExtensionFunctions(defaultConfiguration);
		xPathCache = createXPathCache(defaultConfiguration);
		
		
		final Configuration extractConfiguration = configurationFactory.createConfiguration();
		// disable validation to allow usage of same configuration for any file type (attribute defaults are already expanded)
		extractConfiguration.setSchemaValidationMode(Validation.STRIP);	
		this.extractTransformerCache	= new XslTransformerCache(extractConfiguration);
	}

	/* used by OT */
	public BookCache(URL rootDocumentUrl, BookCacheInitializer initializer, Configuration baseConfiguration, URL ditaOtUrl) {
		registerExtensionFunctions(baseConfiguration);

		this.rootDocumentUrl 			= rootDocumentUrl;
		this.initializer				= initializer;
		this.configurationFactory		= null;
		this.defaultConfiguration		= baseConfiguration;
		this.documentBuilder			= new SaxonDocumentBuilder(defaultConfiguration);
		this.ditaOtUrl				= ditaOtUrl;

		xPathCache = createXPathCache(defaultConfiguration);

		this.extractTransformerCache	= new XslTransformerCache(defaultConfiguration);
	}
	

	public void fillCache(ProgressListener progressListener) {
		cacheProgressListener 	= progressListener;
		try {
			final long startTime = Calendar.getInstance().getTimeInMillis();
			
			if (initializer != null) {
				initializer.initBookCache(this);
			}
			
			final Source 	source 	= defaultConfiguration.getURIResolver().resolve(rootDocumentUrl.toString(), "");
			final FileCache	file	= createFileCache(source);
			if (file != null) {
				file.parse();
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
	
	@Override
	public Configuration createConfiguration() {
		final Configuration configuration = configurationFactory.createConfiguration();
		registerExtensionFunctions(configuration);
		return configuration;
	}
	
	public static XPathCache createXPathCache(Configuration configuration) {
		XPathCache xPathCache = new XPathCache(configuration);
		xPathCache.declareNamespace(KeyDef.NAMESPACE_PREFIX, KeyDef.NAMESPACE_URI);
		return xPathCache;
	}
	
	public URL getRootDocumentUrl() {
		return rootDocumentUrl;
	}
	
	public boolean isUrlIncluded(URL url) {
		final String urlDecoded = FileUtil.decodeUrl(url);
		if (urlDecoded == null) {
			//logger.info("isUrlIncluded(" + url + ") -> null");
			return false;
		} else {
			//logger.info("isUrlIncluded(" + url + ") -> '" + urlDecoded + "', " + decodedUrlList.contains(urlDecoded));
			return fileByUrl.containsKey(urlDecoded);
		}
	}

	public static Configuration createBaseConfiguration() {
		return SaxonConfigurationFactory.loadConfiguration(XsltConref.class.getResource(CONFIG_FILE_URL));
	}
	
	protected void registerExtensionFunctions(Configuration configuration) {
		configuration.registerExtensionFunction(new AncestorPathDef(this));
	}

	public FileCache createFileCache(Source source) {
		final String 	decodedUrl	= FileUtil.decodeUrl(source.getSystemId());
		FileCache 		cachedFile	= null;

		if (fileByUrl.containsKey(decodedUrl)) {
			logger.error("ERROR: File included twice: '" + decodedUrl + "' - ignored second one.");
		} else {
			try {
				// remove the provided xml reader to force saxon creating its own one using the configuration and, thus, expanding the attribute defaults
				if (source instanceof SAXSource) {
					((SAXSource)source).setXMLReader(null);
				}

				final XdmNode 				rootNode 		= documentBuilder.build(source);
				final XdmSequenceIterator 	iterator 		= rootNode.axisIterator(Axis.CHILD);
				
				//logger.info(SaxonNodeWrapper.serializeNode(rootNode.getUnderlyingNode()));
				
				XdmNode rootElement = null;
				while ((iterator.hasNext()) && (rootElement == null)) {
					final XdmNode node =(XdmNode)iterator.next();
					//logger.info("  node: " + node.getNodeName() + ", " + node.getNodeKind());
					if (node.getNodeKind() == XdmNodeKind.ELEMENT) {
						rootElement = node;
					}
				}
				if (rootElement == null) {
					throw new Exception("No root element.");
				}
				
				//logger.info("rootElement: " + rootElement.getNodeName());
				
				cachedFile = new FileCache(decodedUrl, rootElement, this);
				fileByUrl.put(decodedUrl, cachedFile);	// first insert file into map before parsing it to avoid recursions when the cache is tried to be accessed during parsing.

				if (cacheProgressListener != null) {
					final int currFileCount = fileByUrl.size();
					if ((cachedFileCount > 0) && (cachedFileCount < currFileCount)) {
						cachedFileCount = 0;	// file count must have changed -> don't guess it
					}
					cacheProgressListener.setProgress(currFileCount, cachedFileCount);
				}
				//logger.info("created file: '" + decodedUrl + "'");
				
			} catch (Exception e) {
				logger.error("Error parsing file '" + decodedUrl +"':");
				logger.error(e, e);
				cachedFile = null;
			}
		}
		return cachedFile;
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
		keyDefByLocation.clear();
		keyTypeDefByName.clear();
		fileByUrl.clear();
		topicRefByUrl.clear();
		
		extractTransformerCache.clear();
		
		if (documentBuilder instanceof SaxonCachedDocumentBuilder) {
			((SaxonCachedDocumentBuilder)documentBuilder).clearCache();
		}

		fillCache(progressListener);
	}

	public void addKeyDef(KeyDefInterface keyDef) {
		keyDefByRefString.put(keyDef.getRefString(), keyDef);
		keyDefByLocation.put(keyDef.getDefLocation(), keyDef);
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
		final NodeWrapper parent = getParent(node);
		if (parent != null) {
			final String		id		= parent.getAttribute(DitaUtil.ATTR_ID, null);
			KeyDefInterface 	keyDef 	= null; 
			if ((id != null) && (!id.isEmpty())) {
				final String location = DitaUtil.getNodeLocation(parent.getBaseUrl(), id);
				keyDef = keyDefByLocation.get(location);
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

	private NodeWrapper getParent(NodeWrapper node) {
		NodeWrapper parent = node.getParent();
		if (parent == null) {
			final String 			decodedUrl 		= FileUtil.decodeUrl(node.getBaseUrl());
			final TopicRef	parentTopicRef	= getParentTopicRef(decodedUrl);
			if (parentTopicRef != null) {
				//logger.info("parent: " + parentTopicRef.getReferencedFile().getRootWrapper().getName());
				return parentTopicRef.getReferencedFile().getRootWrapper();
			} else {
				return null;
			} 
		} else {
			return parent;
		}
	}

	private TopicRef getParentTopicRef(TopicRef topicRef) {
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
			final TopicRef parentTopicRef = getParentTopicRef(topicRef);
			if (parentTopicRef != null) {
				num = getTopicNum(parentTopicRef);
				if (num != null) {
					num.append(DitaUtil.TOPIC_NUM_DELIMITER);
					num.append(topicRef.getLocalNum());
				}
			} else {
				final String localNum = topicRef.getLocalNum();
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
			final XdmNode 				rootNode = documentBuilder.build(source);
			final XdmSequenceIterator 	iterator = rootNode.axisIterator(Axis.CHILD, BookCache.NAME_KEY_TYPE_DEF_LIST);
			
			while (iterator.hasNext()) {
				createKeyTypeDef((XdmNode)iterator.next(), KeyTypeDef.DEFAULT);
			}
		} catch (Exception e) {
			logger.error("Error parsing KeyTypeDef file '" + FileUtil.decodeUrl(source.getSystemId()) + "':");
			logger.error(e, e);
		}
	}
}
