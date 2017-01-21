package org.DitaSemia.Oxygen;

import java.net.URL;
import java.util.HashMap;

import net.sf.saxon.Configuration;

import org.DitaSemia.Base.ConfigurationInitializer;
import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.Log4jErrorListener;
import org.DitaSemia.Base.ProgressListener;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.DitaSemia.Base.XsltConref.XsltConrefCache;
import org.DitaSemia.Oxygen.AdvancedKeyRef.CustomFunctions.AncestorPath;
import org.apache.log4j.Logger;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.util.editorvars.EditorVariables;

public class BookCacheHandler implements BookCacheProvider, ConfigurationInitializer {
	
	private static final Logger logger = Logger.getLogger(BookCacheHandler.class.getName());
	
	private static BookCacheHandler instance;
	
	private final HashMap<String, BookCache> 	bookCacheMap;
	private final URL							ditaOtUrl;
	private final String						language;
	private final XsltConrefCache				xsltConrefCache;
	private URL									globalKeyTypeDefUrl;
	
	public static BookCacheHandler getInstance() {
		if (instance == null) {
			instance = new BookCacheHandler();
		}
		return instance;
	}
	
	private BookCacheHandler() {
		bookCacheMap 		= new HashMap<>();
		ditaOtUrl			= EditorVariables.expandEditorVariablesAsURL(EditorVariables.CONFIGURED_DITA_OT_DIR_URL + "/", "");
		language			= PluginWorkspaceProvider.getPluginWorkspace().getUserInterfaceLanguage();
		xsltConrefCache 	= new XsltConrefCache(this, this);
		globalKeyTypeDefUrl = null;
		//logger.info("ditaOtUrl: " + ditaOtUrl);
		
		//logger.info("new DocumentCacheHandler(" + initializer + ")");
		
		OxyXPathHandler.getInstance().registerCustomFunction(new AncestorPath());
	}

	public URL getGlobalKeyTypeDefUrl() {
		return globalKeyTypeDefUrl;
	}
	
	public void setGlobalKeyTypeDefUrl(URL url) {
		globalKeyTypeDefUrl = url;
	}
	
	private BookCache createBookCache(URL url, ProgressListener progressListener) {
	
		final BookCache bookCache = new BookCache(url,  this, xsltConrefCache, true, ditaOtUrl, globalKeyTypeDefUrl, language);
		bookCacheMap.put(FileUtil.decodeUrl(url), bookCache);
		bookCache.fillCache(progressListener);	// first insert cache into map before populating it to avoid recursions when the cache is tried to be accessed during populating it. 
		return bookCache;
	}
	
	/*
	 * If there is a map selected in the DITA Maps Manager and this map contains (directly or indirectly) the given file, then this cache is returned.
	 * Otherwise it returns the Cache only for the given file. If none exists yet, a new one is created. 
	 */
	@Override
	public synchronized BookCache getBookCache(URL url) {
		final URL currMapUrl = getCurrMapUrl();
		
		if (currMapUrl != null) {
			final String 	currMapDecodedUrl 	= FileUtil.decodeUrl(currMapUrl);
			BookCache 		mapCache 			= bookCacheMap.get(currMapDecodedUrl);
			if (mapCache == null) {
				//logger.info("create BookCache 1: " + currMapUrl);
				mapCache = createBookCache(currMapUrl, null);
			}
			if ((mapCache != null) && (mapCache.isUrlIncluded(url))) {
				return mapCache;
			}
		}
		final String 	decodedUrl 	= FileUtil.decodeUrl(url);
		BookCache 	fileCache 	= bookCacheMap.get(decodedUrl);
		if (fileCache == null) {
			//logger.info("create BookCache 2: " + url);
			fileCache = createBookCache(url, null);
		}
		return fileCache;
	}

	@Override
	public void initConfig(Configuration configuration) {
		OxySaxonConfigurationFactory.adaptConfiguration(configuration);
		configuration.setErrorListener(new Log4jErrorListener(logger));
	}

	public synchronized void refreshBookCache(URL url, ProgressListener progressListener) {
		final URL currMapUrl = getCurrMapUrl();
		
		BookCache 	bookCache = null;
		if (currMapUrl != null) {
			final String currMapDecodedUrl 	= FileUtil.decodeUrl(currMapUrl);
			bookCache = bookCacheMap.get(currMapDecodedUrl);
			if (bookCache == null) {
				//logger.info("create BookCache 3: " + currMapUrl);
				bookCache = createBookCache(currMapUrl, progressListener);
			} else {
				bookCache.fullRefresh(progressListener);
			}
		}
		if ((bookCache == null) || (!bookCache.isUrlIncluded(url))) {
			final String 	decodedUrl 		= FileUtil.decodeUrl(url);
			BookCache 		fileBookCache 	= bookCacheMap.get(decodedUrl);
			if (fileBookCache == null) {
				//logger.info("create BookCache 4: " + url);
				fileBookCache = createBookCache(url, progressListener);
			} else {
				fileBookCache.fullRefresh(progressListener);
			}
		}

	}
	
	private URL getCurrMapUrl() {
		final WSEditor 	editor 		= PluginWorkspaceProvider.getPluginWorkspace().getCurrentEditorAccess(PluginWorkspace.DITA_MAPS_EDITING_AREA);
		return (editor != null) ? editor.getEditorLocation() : null;
	}

}
