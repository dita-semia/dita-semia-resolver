package org.DitaSemia.Oxygen;

import java.net.URL;
import java.util.HashMap;

import net.sf.saxon.Configuration;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.DocumentCache;
import org.DitaSemia.Base.DocumentCacheInitializer;
import org.DitaSemia.Base.DocumentCacheProvider;
import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.Log4jErrorListener;
import org.DitaSemia.Base.SaxonConfigurationFactory;
import org.DitaSemia.Oxygen.AdvancedKeyRef.CustomFunctions.AncestorPath;
import org.apache.log4j.Logger;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.util.editorvars.EditorVariables;

public class DocumentCacheHandler implements DocumentCacheProvider {
	
	private static final Logger logger = Logger.getLogger(DocumentCacheHandler.class.getName());
	
	private static DocumentCacheHandler instance;
	
	private final HashMap<String, DocumentCache> 	documentCacheMap;
	private final URL								ditaOtUrl;
	
	private DocumentCacheInitializer 	initializer;
	
		
	public static DocumentCacheHandler getInstance() {
		if (instance == null) {
			instance = new DocumentCacheHandler(null);
		}
		return instance;
	}
	
	private DocumentCacheHandler(DocumentCacheInitializer initializer) {
		this.documentCacheMap 	= new HashMap<>();
		this.initializer		= initializer;
		this.ditaOtUrl		= EditorVariables.expandEditorVariablesAsURL(EditorVariables.CONFIGURED_DITA_OT_DIR_URL + "/", "");
		//logger.info("ditaOtUrl: " + ditaOtUrl);
		
		//logger.info("new DocumentCacheHandler(" + initializer + ")");
		
		OxyXPathHandler.getInstance().registerCustomFunction(new AncestorPath());
	}

	public DocumentCacheInitializer getInitializer() {
		return initializer;
	}
	
	public void setInitializer(DocumentCacheInitializer initializer) {
		this.initializer = initializer;
	}
	
	private DocumentCache createDocumentCache(URL url) {
		final SaxonConfigurationFactory configurationFactory = new SaxonConfigurationFactory() {
			@Override
			public Configuration createConfiguration() {
				final Configuration configuration = DocumentCache.createBaseConfiguration();
				OxySaxonConfigurationFactory.adaptConfiguration(configuration);
				configuration.setErrorListener(new Log4jErrorListener(logger));
				return configuration;
			}
		};
		
		final DocumentCache documentCache = new DocumentCache(url, initializer, configurationFactory, ditaOtUrl);
		documentCacheMap.put(FileUtil.decodeUrl(url), documentCache);
		documentCache.fillCache();	// first insert cache into map before populating it to avoid recursions when the cache is tried to be accessed during populating it. 
		return documentCache;
	}
	
	/*
	 * If there is a map selected in the DITA Maps Manager and this map contains (directly or indirectly) the given file, then this cache is returned.
	 * Otherwise it returns the Cache only for the given file. If none exists yet, a new one is created. 
	 */
	@Override
	public DocumentCache getDocumentCache(URL url) {
		final URL currMapUrl = getCurrMapUrl();
		
		if (currMapUrl != null) {
			final String 	currMapDecodedUrl 	= FileUtil.decodeUrl(currMapUrl);
			DocumentCache 	mapCache 			= documentCacheMap.get(currMapDecodedUrl);
			if (mapCache == null) {
				mapCache = createDocumentCache(currMapUrl);
			}
			if ((mapCache != null) && (mapCache.isUrlIncluded(url))) {
				return mapCache;
			}
		}
		final String 	decodedUrl 	= FileUtil.decodeUrl(url);
		DocumentCache 	fileCache 	= documentCacheMap.get(decodedUrl);
		if (fileCache == null) {
			fileCache = createDocumentCache(url);
		}
		return fileCache;
	}

	public void refreshCache(URL url) {
		final URL currMapUrl = getCurrMapUrl();
		
		DocumentCache 	mapCache = null;
		if (currMapUrl != null) {
			final String currMapDecodedUrl 	= FileUtil.decodeUrl(currMapUrl);
			mapCache = documentCacheMap.get(currMapDecodedUrl);
			if (mapCache == null) {
				mapCache = createDocumentCache(currMapUrl);
			} else {
				mapCache.refresh();
			}
		}
		if ((mapCache == null) || (!mapCache.isUrlIncluded(url))) {
			final String 	decodedUrl 	= FileUtil.decodeUrl(url);
			DocumentCache 	fileCache 	= documentCacheMap.get(decodedUrl);
			if (fileCache == null) {
				fileCache = createDocumentCache(url);
			} else {
				fileCache.refresh();
			}
		}

	}
	
	private URL getCurrMapUrl() {
		final WSEditor 	editor 		= PluginWorkspaceProvider.getPluginWorkspace().getCurrentEditorAccess(PluginWorkspace.DITA_MAPS_EDITING_AREA);
		return (editor != null) ? editor.getEditorLocation() : null;
	}

}
