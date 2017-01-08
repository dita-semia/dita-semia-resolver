package org.DitaSemia.Oxygen;

import java.net.URL;
import java.util.HashMap;

import net.sf.saxon.Configuration;

import org.DitaSemia.Base.BookCache;
import org.DitaSemia.Base.BookCacheInitializer;
import org.DitaSemia.Base.BookCacheProvider;
import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.Log4jErrorListener;
import org.DitaSemia.Base.ProgressListener;
import org.DitaSemia.Base.SaxonConfigurationFactory;
import org.DitaSemia.Oxygen.AdvancedKeyRef.CustomFunctions.AncestorPath;
import org.apache.log4j.Logger;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.util.editorvars.EditorVariables;

public class BookCacheHandler implements BookCacheProvider {
	
	private static final Logger logger = Logger.getLogger(BookCacheHandler.class.getName());
	
	private static BookCacheHandler instance;
	
	private final HashMap<String, BookCache> 	documentCacheMap;
	private final URL							ditaOtUrl;
	private final String						language;
	
	private BookCacheInitializer 	initializer;
	
		
	public static BookCacheHandler getInstance() {
		if (instance == null) {
			instance = new BookCacheHandler(null);
		}
		return instance;
	}
	
	private BookCacheHandler(BookCacheInitializer initializer) {
		this.documentCacheMap 	= new HashMap<>();
		this.initializer		= initializer;
		this.ditaOtUrl			= EditorVariables.expandEditorVariablesAsURL(EditorVariables.CONFIGURED_DITA_OT_DIR_URL + "/", "");
		this.language			= PluginWorkspaceProvider.getPluginWorkspace().getUserInterfaceLanguage();
		//logger.info("ditaOtUrl: " + ditaOtUrl);
		
		//logger.info("new DocumentCacheHandler(" + initializer + ")");
		
		OxyXPathHandler.getInstance().registerCustomFunction(new AncestorPath());
	}

	public BookCacheInitializer getInitializer() {
		return initializer;
	}
	
	public void setInitializer(BookCacheInitializer initializer) {
		this.initializer = initializer;
	}
	
	private BookCache createBookCache(URL url, ProgressListener progressListener) {
		final SaxonConfigurationFactory configurationFactory = new SaxonConfigurationFactory() {
			@Override
			public Configuration createConfiguration() {
				final Configuration configuration = BookCache.createBaseConfiguration();
				OxySaxonConfigurationFactory.adaptConfiguration(configuration);
				configuration.setErrorListener(new Log4jErrorListener(logger));
				return configuration;
			}
		};
		
		final BookCache bookCache = new BookCache(url, initializer, configurationFactory, ditaOtUrl, language);
		documentCacheMap.put(FileUtil.decodeUrl(url), bookCache);
		bookCache.fillCache(progressListener);	// first insert cache into map before populating it to avoid recursions when the cache is tried to be accessed during populating it. 
		return bookCache;
	}
	
	/*
	 * If there is a map selected in the DITA Maps Manager and this map contains (directly or indirectly) the given file, then this cache is returned.
	 * Otherwise it returns the Cache only for the given file. If none exists yet, a new one is created. 
	 */
	@Override
	public BookCache getBookCache(URL url) {
		final URL currMapUrl = getCurrMapUrl();
		
		if (currMapUrl != null) {
			final String 	currMapDecodedUrl 	= FileUtil.decodeUrl(currMapUrl);
			BookCache 	mapCache 			= documentCacheMap.get(currMapDecodedUrl);
			if (mapCache == null) {
				mapCache = createBookCache(currMapUrl, null);
			}
			if ((mapCache != null) && (mapCache.isUrlIncluded(url))) {
				return mapCache;
			}
		}
		final String 	decodedUrl 	= FileUtil.decodeUrl(url);
		BookCache 	fileCache 	= documentCacheMap.get(decodedUrl);
		if (fileCache == null) {
			fileCache = createBookCache(url, null);
		}
		return fileCache;
	}

	public void refreshBookCache(URL url, ProgressListener progressListener) {
		final URL currMapUrl = getCurrMapUrl();
		
		BookCache 	bookCache = null;
		if (currMapUrl != null) {
			final String currMapDecodedUrl 	= FileUtil.decodeUrl(currMapUrl);
			bookCache = documentCacheMap.get(currMapDecodedUrl);
			if (bookCache == null) {
				bookCache = createBookCache(currMapUrl, progressListener);
			} else {
				bookCache.fullRefresh(progressListener);
			}
		}
		if ((bookCache == null) || (!bookCache.isUrlIncluded(url))) {
			final String 	decodedUrl 		= FileUtil.decodeUrl(url);
			BookCache 		fileBookCache 	= documentCacheMap.get(decodedUrl);
			if (fileBookCache == null) {
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
