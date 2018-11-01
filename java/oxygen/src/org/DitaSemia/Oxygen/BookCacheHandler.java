package org.DitaSemia.Oxygen;

import java.net.URL;
import java.util.HashMap;
import javax.xml.transform.URIResolver;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Initializer;

import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.Log4jErrorListener;
import org.DitaSemia.Base.ProgressListener;
import org.DitaSemia.Base.SaxonDocumentBuilder;
import org.DitaSemia.Base.XslTransformerCache;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.DitaSemia.Base.DocumentCaching.FileCache;
import org.DitaSemia.Base.XsltConref.XsltConrefCache;
import org.DitaSemia.Oxygen.AdvancedKeyRef.CustomFunctions.AncestorPath;
import org.DitaSemia.Oxygen.AdvancedKeyRef.CustomFunctions.ExtractText;
import org.apache.log4j.Logger;
import org.apache.commons.io.FilenameUtils;
import org.xml.sax.EntityResolver;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.util.editorvars.EditorVariables;

public class BookCacheHandler implements BookCacheProvider, Initializer {
	
	private static final Logger logger = Logger.getLogger(BookCacheHandler.class.getName());
	
	private static BookCacheHandler instance;
	
	private final HashMap<String, BookCache> 	bookCacheMap;
	private final URL							ditaOtUrl;
	private final String						language;
	private final EntityResolver				entityResolver;
	private final URIResolver					uriResolver;
	private final XslTransformerCache			extractTransformerCache;
	private final SaxonDocumentBuilder			documentBuilder;
	private final XsltConrefCache				xsltConrefCache;
	private URL									globalKeyTypeDefUrl;
	private final String						hddCachePath;
	
	private Thread								refreshThread;
	private static ProgressListener				progressListener;
	
	public static BookCacheHandler getInstance() {
		if (instance == null) {
			instance = new BookCacheHandler();
		}
		return instance;
	}
	
	private BookCacheHandler() {
		bookCacheMap 			= new HashMap<>();
		ditaOtUrl				= EditorVariables.expandEditorVariablesAsURL(EditorVariables.CONFIGURED_DITA_OT_DIR_URL + "/", "");
		language				= PluginWorkspaceProvider.getPluginWorkspace().getUserInterfaceLanguage();
		entityResolver			= PluginWorkspaceProvider.getPluginWorkspace().getXMLUtilAccess().getEntityResolver();
		uriResolver				= PluginWorkspaceProvider.getPluginWorkspace().getXMLUtilAccess().getURIResolver();
		
		final Configuration extractConfiguration = new Configuration();
		SaxonDocumentBuilder.makeConfigurationCompatible(extractConfiguration);
		extractConfiguration.setURIResolver(uriResolver);
		BookCache.registerExtractTextExtensionFunctions(extractConfiguration, this);
		
		extractTransformerCache	= new XslTransformerCache(extractConfiguration);
		documentBuilder			= new SaxonDocumentBuilder(entityResolver, uriResolver);
		xsltConrefCache 		= new XsltConrefCache(this, this, documentBuilder, null, null);
		hddCachePath			= FilenameUtils.concat(System.getProperty("java.io.tmpdir"), "DitaSemiaCache/");
		globalKeyTypeDefUrl	 	= null;
		//logger.info("ditaOtUrl: " + ditaOtUrl);
		
		//logger.info("new DocumentCacheHandler(" + initializer + ")");
		refreshThread 		= new Thread();
		
		OxyXPathHandler.getInstance().registerCustomFunction(new AncestorPath());
		OxyXPathHandler.getInstance().registerCustomFunction(new ExtractText(extractTransformerCache, documentBuilder));
	}

	public URL getGlobalKeyTypeDefUrl() {
		return globalKeyTypeDefUrl;
	}
	
	public void setGlobalKeyTypeDefUrl(URL url) {
		globalKeyTypeDefUrl = url;
	}
	
	private BookCache createBookCache(URL url) {
	
		final BookCache bookCache = new BookCache(
					url,  
					this, 
					xsltConrefCache, 
					documentBuilder, 
					extractTransformerCache, 
					true, 
					false, 
					ditaOtUrl, 
					globalKeyTypeDefUrl,
					hddCachePath,
					language);
		bookCacheMap.put(FileUtil.decodeUrl(url), bookCache);
		bookCache.fillCache(progressListener);	// first insert cache into map before populating it to avoid recursions when the cache is tried to be accessed during populating it. 
		return bookCache;
	}
	
	/*
	 * If there is a map selected in the DITA Maps Manager and this map contains (directly or indirectly) the given file, then this cache is returned.
	 * Otherwise it returns the Cache only for the given file. If none exists yet, a new one is created. 
	 */
	@Override
	public BookCache getBookCache(URL url) {
		if (!refreshThread.isAlive()) {
			final URL currMapUrl = getCurrMapUrl();
			
			if (currMapUrl != null) {
				final String 	currMapDecodedUrl 	= FileUtil.decodeUrl(currMapUrl);
				BookCache 		mapCache 			= bookCacheMap.get(currMapDecodedUrl);
				if (mapCache == null) {
					//logger.info("create BookCache 1: " + currMapUrl);
					mapCache = createBookCache(currMapUrl);
				}
				if ((mapCache != null) && (mapCache.isUrlIncluded(url))) {
					return mapCache;
				}
			}
			final String 	decodedUrl 	= FileUtil.decodeUrl(url);
			BookCache 	fileCache 	= bookCacheMap.get(decodedUrl);
			if (fileCache == null) {
				//logger.info("create BookCache 2: " + url);
				fileCache = createBookCache(url);
			}
			return fileCache;
		} else {
			return null;
		}
	}

	@Override
	public URL getBookCacheRootUrl(URL url) {
		if (!refreshThread.isAlive()) {
			final URL currMapUrl = getCurrMapUrl();
			if (currMapUrl != null) {
				final String 	currMapDecodedUrl 	= FileUtil.decodeUrl(currMapUrl);
				final BookCache mapCache 			= bookCacheMap.get(currMapDecodedUrl);
				if ((mapCache != null) && (mapCache.isUrlIncluded(url))) {
					return currMapUrl;
				}
			}
		}
		return null;
	}

	@Override
	public void initialize(Configuration configuration) {
		OxySaxonConfigurationFactory.adaptConfiguration(configuration);
		configuration.setErrorListener(new Log4jErrorListener(logger));
	}

	public void refreshBookCache(URL url) {
		if (progressListener != null) {
			Runnable task = () -> {
				refreshBookCacheTask(url);
			};
			refreshThread = new Thread(task);
			refreshThread.start();
		} else {
			refreshBookCacheTask(url);
		}
		
	}
	
	private void refreshBookCacheTask(URL url) {
		final URL currMapUrl = getCurrMapUrl();
			
			BookCache 	bookCache = null;
			if (currMapUrl != null) {
				final String currMapDecodedUrl 	= FileUtil.decodeUrl(currMapUrl);
				bookCache = bookCacheMap.get(currMapDecodedUrl);
				if (bookCache == null) {
					//logger.info("create BookCache 3: " + currMapUrl);
					bookCache = createBookCache(currMapUrl);
				} else {
					bookCache.fullRefresh(progressListener);
				}
			}
			if ((bookCache == null) || (!bookCache.isUrlIncluded(url))) {
				final String 	decodedUrl 		= FileUtil.decodeUrl(url);
				BookCache 		fileBookCache 	= bookCacheMap.get(decodedUrl);
				if (fileBookCache == null) {
					//logger.info("create BookCache 4: " + url);
					fileBookCache = createBookCache(url);
				} else {
					fileBookCache.fullRefresh(progressListener);
				}
			}
			//TODO 
			//refreshAllDisplays();
	}
	
	public void refreshFileCache(URL url) {
		BookCache bookCache = getBookCache(url);
		if (getBookCache(url) != null) {
			final FileCache	fileCache	= bookCache.getFile(url);
			if (!fileCache.isUpdated()) {
				bookCache.partialRefresh(url);
				//refreshDisplay(url);
			}
		}
	}
	
	/*private void refreshAllDisplays() {
		URL[] openFiles = PluginWorkspaceProvider.getPluginWorkspace().getAllEditorLocations(PluginWorkspace.MAIN_EDITING_AREA);
		for (URL file : openFiles) {
			refreshDisplay(file);
		}
	}*/
	/*
	private void refreshDisplay(URL fileUrl) {
//		final long startTime = Calendar.getInstance().getTimeInMillis();
//		logger.info("refreshDisplay " + fileUrl);
		WSEditor editorAccess = PluginWorkspaceProvider.getPluginWorkspace().getEditorAccess(fileUrl, PluginWorkspace.MAIN_EDITING_AREA);
		if (editorAccess != null) {
			WSEditorPage currentPage = editorAccess.getCurrentPage();
		    if (currentPage instanceof WSAuthorEditorPage) {
		    	List<AuthorNode> nodes = new CopyOnWriteArrayList<>();
		    	List<AuthorNode> contentNodes = ((WSAuthorEditorPage) currentPage).getDocumentController().getAuthorDocumentNode().getContentNodes();
		    	nodes.addAll(contentNodes);
		    	while (!nodes.isEmpty()) {
		    		AuthorNode node = nodes.get(0);
	    			if (node instanceof AuthorParentNode) {
	    				if ((node.getDisplayName().equals("key-xref") || node.getDisplayName().equals("xref"))) {
		    				//logger.info("node: " + node.getDisplayName());
		    				((WSAuthorEditorPage) currentPage).refresh(node);
	    				}
	    				nodes.addAll(((AuthorParentNode) node).getContentNodes());
	    				nodes.remove(node);
	    			}
	    		}
		    }
		}
//		logger.info("refresh done in " + String.valueOf(Calendar.getInstance().getTimeInMillis() - startTime) + " ms");
	}*/

	public URL getCurrMapUrl() {
		final WSEditor 	editor 		= PluginWorkspaceProvider.getPluginWorkspace().getCurrentEditorAccess(PluginWorkspace.DITA_MAPS_EDITING_AREA);
		return (editor != null) ? editor.getEditorLocation() : null;
	}

	public EntityResolver getEntityResolver() {
		return entityResolver;
	}

	public XsltConrefCache getXsltConrefCache() {
		return xsltConrefCache;
	}
	
	public void clearCache() {
		bookCacheMap.clear();
		extractTransformerCache.clear();
		documentBuilder.clearCache();
		xsltConrefCache.clear();
	}

	public SaxonDocumentBuilder getDocumentBuilder() {
		return documentBuilder;
	}
	
	public URIResolver getUriResolver() {
		return uriResolver;
	}

	public static void setProgressListener(ProgressListener listener) {
		progressListener = listener;
	}
	
	public static void setLastCachingStatistics(String str) {
		
	}
}
