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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Initializer;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.ProgressListener;
import org.DitaSemia.Base.SaxonDocumentBuilder;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.DitaSemia.Base.XPathCache;
import org.DitaSemia.Base.XslTransformerCache;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefListInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyTypeDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyTypeDefListInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyspecInterface;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.AncestorPathDef;
import org.DitaSemia.Base.ExtensionFunctions.ExtractTextDef;
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
	
	
	private HashMap<String, KeyDefInterface> 		keyDefByRefString 	= new HashMap<>();
	private HashMap<String, Set<KeyDefInterface>> 	keyDefByTexts 		= new HashMap<>();
	private HashMap<String, KeyDefInterface> 		keyDefByTypeName	= new HashMap<>();
	private HashMap<String, Set<KeyDefInterface>> 	ambiguousKeyDefs	= new HashMap<>();
	private HashMap<String, Stack<KeyDefInterface>> overwrittenKeyDefs	= new HashMap<>();
	private HashMap<String, KeyDefInterface> 		keyDefByUrlAndId	= new HashMap<>();
	private HashMap<String, KeyTypeDef> 			keyTypeDefByName	= new HashMap<>();
	private HashMap<String, FileCache> 				fileByUrl			= new HashMap<>();
	private HashMap<String, TopicRef> 				topicRefByUrl		= new HashMap<>();
	private HashMap<String, Set<FileCache>> 		filesByTopicId		= new HashMap<>();	// to identify ambiguous topic ids
	private Map<String, Set<NodeInfo>>				keyRefsByRefString	= new HashMap<>();
	private Set<FileCache>							unparsedFiles		= new HashSet<>();

	private final URL 						rootDocumentUrl;
	private final Initializer				configurationInitializer;
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

	private final Queue<NeedsInit>			initQueue 			= new PriorityQueue<>(100, new NeedsInit.PriorityComparator());
	private ProgressListener				cachedProgressListener;
	private int								cachedFileCount		= 0;

	
	public BookCache(
			URL 					rootDocumentUrl,
			Initializer				configurationInitializer,
			XsltConrefCache 		xsltConrefCache,
			SaxonDocumentBuilder	documentBuilder,
			XslTransformerCache		extractTransformerCache,
			boolean					expandAttributeDefaults,
			URL 					ditaOtUrl,
			URL 					globalKeyTypeDefUrl, 
			String					hddCachePath,
			String 					language) {
		
		this.rootDocumentUrl 			= rootDocumentUrl;
		this.configurationInitializer	= configurationInitializer;
		this.xsltConrefCache			= xsltConrefCache;
		this.documentBuilder			= documentBuilder;
		this.extractTransformerCache	= extractTransformerCache;
		this.expandAttributeDefaults	= expandAttributeDefaults;
		this.defaultConfiguration		= createConfiguration(null); // needs to be done after this.configurationInitializer has been set
		
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
//		logger.info("fillCache");
//		cacheProgressListener 	= progressListener;
		if (cachedProgressListener == null) {
			cachedProgressListener = progressListener;
		}
		try {
			final long startTime = Calendar.getInstance().getTimeInMillis();
			if (progressListener != null) {
				progressListener.setProgress(0, cachedFileCount);
			}
			
			if (globalKeyTypeDefUrl != null) {
				parseKeyTypeDefFile(globalKeyTypeDefUrl);
			}
			
			final Source 	source 	= defaultConfiguration.getURIResolver().resolve(rootDocumentUrl.toString(), "");
			//logger.info("url: " + rootDocumentUrl.toString());
			initQueue.clear();
			createFileCache(source, null, false);
			if (!initQueue.isEmpty()) {
				int progress = 0;
				int total = cachedFileCount;
				while (!initQueue.isEmpty()) {
					final NeedsInit needsInit = initQueue.poll();
					needsInit.init();
					
					progress++;
					if (needsInit.getPriority() > 0) {
						total = progress + initQueue.size();
					} 
					if (progressListener != null) {
						progressListener.setProgress(progress, total);
					}
					
					if (needsInit instanceof FileCache) {
						final FileCache fileCache = (FileCache)needsInit;
						if (!fileCache.isFileParsed()) {
							unparsedFiles.add(fileCache);
						}
					}
				}
				
				logCachingStatistics("fill Cache", Calendar.getInstance().getTimeInMillis() - startTime, FileUtil.decodeUrl(source.getSystemId()));
			}
			cachedFileCount = fileByUrl.size();
		} catch (TransformerException e) {
			logger.error(e);
			//cachedFileCount	= 0;
		} 
		//cacheProgressListener = null;
	}
	
	private void logCachingStatistics(String action, long time, String url) {
		final StringBuilder sb = new StringBuilder();
		sb.append(action);
		sb.append(" done: ");
		sb.append(time);
		sb.append("ms, ");
		sb.append(fileByUrl.size());
		sb.append(" files (");
		sb.append(unparsedFiles.size());
		sb.append(" unparsed), ");
		sb.append(keyDefByRefString.size());
		sb.append(" keys (");
		sb.append(ambiguousKeyDefs.size());
		sb.append(" ambiguous, ");
		sb.append(overwrittenKeyDefs.size());
		sb.append(" overwritten), ");
		sb.append(keyDefByTypeName.size());		
		sb.append(" types (");
		sb.append(url);
		sb.append(")");
		logger.info(sb.toString());
		if (cachedProgressListener != null) {
			String[] statistics = {	String.valueOf(time) + " ms", 
									String.valueOf(fileByUrl.size()), 
									String.valueOf(unparsedFiles.size()), 
									String.valueOf(keyDefByRefString.size()), 
									String.valueOf(ambiguousKeyDefs.size()), 
									String.valueOf(keyDefByTypeName.size()), 
									url};
			cachedProgressListener.setCachingStatistics(statistics);
		}
		
		for (Entry<String, Set<KeyDefInterface>> entrySet: ambiguousKeyDefs.entrySet()) {
			logger.info("ambiguous key-ref-string: '" + entrySet.getKey() + "'");
			for (KeyDefInterface keyDef : entrySet.getValue()) {
				logger.info("  " + keyDef.getDefLocation());
			}
		}
	}
	
	public Configuration createConfiguration(Configuration compatibleConfig) {
		final Configuration configuration = SaxonDocumentBuilder.loadConfiguration(XsltConref.class.getResource(CONFIG_FILE_URL));
		
		configuration.registerExtensionFunction(new AncestorPathDef(this));
		configuration.registerExtensionFunction(new ExtractTextDef(extractTransformerCache));
		
		if (configurationInitializer != null) {
			try {
				configurationInitializer.initialize(configuration);
			} catch (TransformerException e) {
				logger.error(e, e);
			}
		}

		if (compatibleConfig != null) {
			configuration.getDefaultXsltCompilerInfo().setSchemaAware(compatibleConfig.getDefaultXsltCompilerInfo().isSchemaAware());
			configuration.setNamePool(compatibleConfig.getNamePool());
			configuration.setDocumentNumberAllocator(compatibleConfig.getDocumentNumberAllocator());
			final XslTransformerCache transformerCache = new XslTransformerCache(configuration);
			configuration.registerExtensionFunction(new ExtractTextDef(transformerCache));
		} else {
			configuration.registerExtensionFunction(new ExtractTextDef(extractTransformerCache));
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


	public FileCache createFileCache(Source source, String topicrefClass, boolean isResourceOnly) {
		final String 	decodedUrl	= FileUtil.decodeUrl(source.getSystemId());
		FileCache 		fileCache	= null;
		
		//logger.info("Source: " + decodedUrl + ", " + source.getSystemId() + ", " + source.getClass());

		if (fileByUrl.containsKey(decodedUrl)) {
			logger.error("ERROR: File included twice: '" + decodedUrl + "' - ignored second one.");
		} else {
			fileCache = new FileCache(source, (expandAttributeDefaults || isResourceOnly), topicrefClass, isResourceOnly, this);
			fileByUrl.put(decodedUrl, fileCache);
			initQueue.add(fileCache);
		}
		return fileCache;
	}

	public TopicRef createTopicRef(FileCache containingFile, FileCache refFile, TopicRefContainer parentTopicRefContainer, boolean isResourceOnly, NodeWrapper nodeWrapper) {
		final TopicRef topicRef = new TopicRef(containingFile, refFile, parentTopicRefContainer, isResourceOnly, nodeWrapper);
		if (refFile != null) {
			topicRefByUrl.put(refFile.getDecodedUrl(), topicRef);
		}
		return topicRef;
	}
	
	
	public void fullRefresh(ProgressListener progressListener) {
//		logger.info("fullRefresh");
		
		keyDefByRefString.clear();
		keyDefByTypeName.clear();
		ambiguousKeyDefs.clear();
		overwrittenKeyDefs.clear();
		keyDefByTexts.clear();
		keyDefByUrlAndId.clear();
		keyTypeDefByName.clear();
		fileByUrl.clear();
		topicRefByUrl.clear();
		keyRefsByRefString.clear();
		filesByTopicId.clear();
		unparsedFiles.clear();
		
		fillCache(progressListener);
	}
	
	
	public void partialRefresh(URL fileUrl) {
		partialRefresh(getFile(fileUrl));
	}
	
	
	protected void partialRefresh(FileCache fileCache) {

		final long startTime = Calendar.getInstance().getTimeInMillis();
		
		final Collection<KeyDefInterface> 	keyDefs 	= fileCache.getKeyDefList();
		for (KeyDefInterface keyDef : keyDefs) {
			final String refString = keyDef.getRefString();
			removeKeyDefFromMaps(keyDef);
			Set<KeyDefInterface> listAmbiguous = ambiguousKeyDefs.get(refString);
			if (listAmbiguous != null) {
				listAmbiguous.remove(keyDef);
				if (listAmbiguous.size() <= 1) {
					ambiguousKeyDefs.remove(refString);	// no more ambiguous
				}
			}
			
			Stack<KeyDefInterface> listOverwritten = overwrittenKeyDefs.get(refString);
			if ((listOverwritten != null) && (!listOverwritten.isEmpty())) {
				final KeyDefInterface overwrittenKeyDef = listOverwritten.pop();
				if (listOverwritten.isEmpty()) {
					overwrittenKeyDefs.remove(refString);
				}
				//logger.info("activating overwritten KeyDef: " + refString + ", " + overwrittenKeyDef.getDefUrl());
				AddKeyDefToMaps(overwrittenKeyDef);
			}
		}
		for (Entry<String, Set<NodeInfo>> entry: fileCache.getKeyRefs().entrySet()) {
			final Set<NodeInfo> set = keyRefsByRefString.get(entry.getKey());
			if (set != null) {
				set.removeAll(entry.getValue());
			}
		}
		
		for (String topicId : fileCache.getTopicIds()) {
			final Set<FileCache> set = filesByTopicId.get(topicId);
			if (set != null) {
				set.remove(fileCache);
			}
		}
		
		fileCache.refresh();
		
		if (fileCache.isFileParsed()) {
			unparsedFiles.remove(fileCache);
		}
		
		logCachingStatistics("Refresh file cache", Calendar.getInstance().getTimeInMillis() - startTime, fileCache.getDecodedUrl());
	}
	
	private void removeKeyDefFromMaps(KeyDefInterface keyDef) {
		keyDefByRefString.remove(keyDef.getRefString(), keyDef);
		removeKeyDefByTypeName(keyDef);
		removeKeyByTexts(keyDef);
		removeKeyDefByUrlAndId(keyDef);
	}

	private void removeKeyDefByTypeName(KeyDefInterface keyDef) {
		final String typeName = keyDef.getDxdTypeName();
		if (typeName != null) {
			keyDefByTypeName.remove(typeName, keyDef);
		}
	}

	private void addKeyDefByTypeName(KeyDefInterface keyDef) {
		final String typeName = keyDef.getDxdTypeName();
		if (typeName != null) {
			if (keyDefByTypeName.containsKey(typeName)) {
				logger.warn("Ambiguous typeName '" + typeName + "'");
			} else {
				keyDefByTypeName.put(typeName, keyDef);
			}
		}
	}

	private void removeKeyDefByUrlAndId(KeyDefInterface keyDef) {
		final URL 		defUrl 	= keyDef.getDefUrl();
		final String	defId 	= keyDef.getDefId();
		if ((defUrl != null) && (defId != null)) {
			keyDefByUrlAndId.remove(FileUtil.decodeUrl(defUrl) + DitaUtil.HREF_URL_ID_DELIMITER + keyDef.getDefId(), keyDef);
		}
	}

	private void addKeyDefByUrlAndId(KeyDefInterface keyDef) {
		final URL 		defUrl 	= keyDef.getDefUrl();
		final String	defId 	= keyDef.getDefId();
		if ((defUrl != null) && (defId != null)) {
			keyDefByUrlAndId.put(FileUtil.decodeUrl(defUrl) + DitaUtil.HREF_URL_ID_DELIMITER + keyDef.getDefId(), keyDef);
		}
	}

	private void removeKeyByTexts(KeyDefInterface keyDef) {
		List<String> 	namespace 	= keyDef.getNamespaceList();
		String 			key 		= keyDef.getKey();
		String 			text		= key;
		removeKeyByText(text, keyDef);
		while(!namespace.isEmpty()) {
			text = namespace.remove(namespace.size() - 1) + KeyDef.PATH_DELIMITER + text;
			removeKeyByText(text, keyDef);
		}
	}
	
	private void removeKeyByText(String text, KeyDefInterface keyDef) {
		Set<KeyDefInterface> list = keyDefByTexts.get(text);
		if (list != null) {
			list.remove(keyDef);
		}
		keyDefByTexts.put(text, list);
	}

	public void addKeyRef(String refString, NodeInfo node) {
		//logger.info("addKeyRef: " + refString);
		Set<NodeInfo> keyRefs = keyRefsByRefString.get(refString);
		if (keyRefs == null) {
			keyRefs = new HashSet<>();
			keyRefsByRefString.put(refString, keyRefs);
		} 
		keyRefs.add(node);
	}
	
	public void addKeyDef(KeyDefInterface keyDef) {
		//logger.info("addKeyDef: " + keyDef.toString());
		final String 			refString 		= keyDef.getRefString();
		final KeyDefInterface 	existingKeyDef 	= keyDefByRefString.get(refString);
		//logger.info("  existingKeyDef: " + existingKeyDef);
		if ((existingKeyDef != null) && (!existingKeyDef.isOverwritable())) {
			Set<KeyDefInterface> list = ambiguousKeyDefs.get(refString);
			if (list == null) {
				list = new HashSet<KeyDefInterface>();
				ambiguousKeyDefs.put(refString, list);
			}
			list.add(keyDef);

			/*try {
				throw new Exception("ambigous keydef: " + refString);
			} catch (Exception e) {
				logger.error(e, e);
			}*/
		} else {
			if (existingKeyDef != null) {

				Stack<KeyDefInterface> stack = overwrittenKeyDefs.get(refString);
				if (stack == null) {
					stack = new Stack<KeyDefInterface>();
					overwrittenKeyDefs.put(refString, stack);
				}
				stack.add(existingKeyDef);
				//logger.info("overwriting KeyDef: " + refString + ", " + existingKeyDef.getDefUrl() + " by " + keyDef.getDefUrl());
				removeKeyDefFromMaps(existingKeyDef);
			}

			AddKeyDefToMaps(keyDef);
			
			//logger.info("typeName: " + typeName + "(" + keyDef.getRefString() + ")");
		}
	}

	private void AddKeyDefToMaps(KeyDefInterface keyDef) {
		keyDefByRefString.put(keyDef.getRefString(), keyDef);
		addKeyByTexts(keyDef);
		addKeyDefByUrlAndId(keyDef);
		addKeyDefByTypeName(keyDef);
	}

	private void addKeyByTexts(KeyDefInterface keyDef) {
		List<String> 	namespace 	= keyDef.getNamespaceList();
		String 			key 		= keyDef.getKey();
		String 			text		= key;
		addKeyByText(text, keyDef);
		while(!namespace.isEmpty()) {
			text = normalizeKeyText(namespace.remove(namespace.size() - 1)) + KeyDef.PATH_DELIMITER + text;
			addKeyByText(text, keyDef);
		}
	}
	
	private String normalizeKeyText(String text) {
		return text.replace('.', KeyDef.PATH_DELIMITER.charAt(0));
	}

	private void addKeyByText(String text, KeyDefInterface keyDef) {
		Set<KeyDefInterface> list = keyDefByTexts.get(text);
		if (list != null) {
			list.add(keyDef);
		} else {
			list = new HashSet<>();
			list.add(keyDef);
		}
		keyDefByTexts.put(text, list);
	}
	
	public Collection<NodeInfo> getKeyRefs(String refString) {
		ensureAllFilesParsed();
		return keyRefsByRefString.get(refString);
	}
	
	public void ensureAllFilesParsed() {
		if (!unparsedFiles.isEmpty()) {
			for (FileCache fileCache: unparsedFiles) {
				fileCache.ensureFileIsParsed();
			}
			unparsedFiles.clear();
		}
	}
	
	
	public void ensureAllFilesUpdated() {
		for (FileCache fileCache: fileByUrl.values()) {
			if (!fileCache.isUpdated()) {
				partialRefresh(fileCache);
			}
		}
	}
	
	
	public boolean isAllFilesParsed() {
		return (unparsedFiles.isEmpty());
	}
	
	public Collection<KeyDefInterface> getKeyDefListByText(String text) {
		return keyDefByTexts.get(normalizeKeyText(text));
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
				//logger.info("  location: '" + location + "', keyDef: " + keyDef + ", KeyType: " + ((keyDef == null) ? null : keyDef.getType()));
				if ((keyDef != null) && (keyType != null)) {
					//logger.info("keyType: '" + keyType + "', keyDef.getType(): '" + keyDef.getType() + "', equals: " + keyType.equals(keyDef.getType()));
				}
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

	public String getTopicNum(String decodedUrl, String topicId) {
		final FileCache fileCache = fileByUrl.get(decodedUrl);
		if (fileCache != null) {
			final StringBuffer topicNum = fileCache.getTopicNum(topicId);
			if (topicNum != null) {
				return topicNum.toString();
			}
		}
		return null;
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
			final XdmNode 				rootNode = documentBuilder.build(source, false, true);
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

	@Override
	public Collection<KeyDefInterface> getAmbiguousKeyDefs(String refString) {
		return ambiguousKeyDefs.get(refString);
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
			//logger.info("getNodeByLocation: url = '" + url + "', id = '" + id + "'");
			final FileCache fileCache = fileByUrl.get(url);
			//logger.info("  fileCache: " + fileCache);
			if (fileCache != null) {
				return fileCache.getElementByRefId(id);
			}
		}
		return null;
	}

	public void addNeedsInit(NeedsInit needsInit) {
		initQueue.add(needsInit);
	}

	public KeyDefInterface getKeyDefByTypeName(String typeName) {
		return keyDefByTypeName.get(typeName);
	}

	public void addTopicId(String id, FileCache fileCache) {
		Set<FileCache> fileList = filesByTopicId.get(id);
		if (fileList == null) {
			fileList = new HashSet<>();
			filesByTopicId.put(id, fileList);
		}
		fileList.add(fileCache);
	}

	public Collection<FileCache> getFilesByTopicId(String topicId) {
		return filesByTopicId.get(topicId);
	}

}
