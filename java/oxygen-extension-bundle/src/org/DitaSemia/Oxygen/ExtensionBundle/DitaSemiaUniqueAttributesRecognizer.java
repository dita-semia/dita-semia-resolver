package org.DitaSemia.Oxygen.ExtensionBundle;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.FileCache;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.BookCacheHandler;
import org.DitaSemia.Oxygen.Conbat.ConbatStylesFilter;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.commons.id.GenerateIDElementsInfo;
import ro.sync.ecss.extensions.dita.id.DITAUniqueAttributesRecognizer;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.listeners.WSEditorListener;

public class DitaSemiaUniqueAttributesRecognizer extends DITAUniqueAttributesRecognizer {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DitaSemiaUniqueAttributesRecognizer.class.getName());
	
	public static final String ID_PATTERN 			= "${id}";
	public static final String CBA_FLAG_NEEDS_ID	= "needs-id";
	
	protected final Set<String>	autoIdClassList = new HashSet<>();
	
	private boolean active = true;
	
	public DitaSemiaUniqueAttributesRecognizer() {
		//logger.info("DitaSemiaUniqueAttributesRecognizer");
		autoIdClassList.add("topic/topic");
		autoIdClassList.add("topic/section");
		autoIdClassList.add("topic/table");
		autoIdClassList.add("topic/row");
		autoIdClassList.add("topic/simpletable");
		autoIdClassList.add("topic/strow");
		autoIdClassList.add("topic/dl");
		autoIdClassList.add("topic/dlentry");
		autoIdClassList.add("topic/ul");
		autoIdClassList.add("topic/ol");
		autoIdClassList.add("topic/li");
		autoIdClassList.add("topic/sl");
		autoIdClassList.add("topic/sli");
		autoIdClassList.add("topic/image");
		autoIdClassList.add("topic/fig");
		autoIdClassList.add("topic/p");
		autoIdClassList.add("gc-d/needs-id");
	}
	
	/**
	 * Generate an unique ID for an element
	 * @param idGenerationPattern The pattern for id generation.
	 * @param element The element
	 * @return The unique ID
	 */
	@Override
	protected String generateUniqueIDFor(String idGenerationPattern, AuthorElement element) {
		if (active) {
			return generateUniqueIDFor(element);
		} else {
			return "";
		}		
	}

	protected String generateUniqueIDFor(AuthorElement element) {
		if (active) {
			return GenerateIDElementsInfo.generateID(ID_PATTERN, "").replace("_", "-").toUpperCase();
		} else {
			return "";
		}
	}

	@Override
	protected String getGenerateIDAttributeQName(AuthorElement element, String[] elemsWithAutoGeneration, boolean forceGeneration) {
		if (active) {
			AuthorNodeWrapper node = new AuthorNodeWrapper(element, null);
			
			/*logger.info("elemsWithAutoGeneration:");
			for (int i = 0; i < elemsWithAutoGeneration.length; ++i) {
				logger.info("  '" + elemsWithAutoGeneration[i] + "'");
			}*/
			
			final String cbaFlagsAttr = node.getAttribute(ConbatStylesFilter.ATTR_FLAGS, ConbatStylesFilter.NAMESPACE_URI);
			//logger.info("cbaFlagsAttr: '" + cbaFlagsAttr + "'");
			if ((cbaFlagsAttr != null) && (!cbaFlagsAttr.isEmpty())) {
				final StringTokenizer st = new StringTokenizer(cbaFlagsAttr, " ", false);
				while (st.hasMoreTokens()) {
					final String nextToken = st.nextToken();
					if (nextToken.equals(CBA_FLAG_NEEDS_ID)) {
						//logger.info(" -> yes");
						return idAttrQname;
					}
				}
			}
			
			final String classAttr = node.getAttribute(DitaUtil.ATTR_CLASS, null);
			//logger.info("classAttr: '" + classAttr + "'");
			if ((classAttr != null) && (!classAttr.isEmpty())) {
				final StringTokenizer st = new StringTokenizer(classAttr, " ", false);
				while (st.hasMoreTokens()) {
					final String nextToken = st.nextToken();
					if (autoIdClassList.contains(nextToken)) {
						//logger.info(" -> yes");
						return idAttrQname;
					}
				}
			}
			
			//logger.info(" -> no");
			return null;
		} else {
			return null;
		}
	}
	
	private WSEditorChangeListener 	editorChangeListener 	= null;
	private WSEditorListener 		editorListener 			= null;
	
	@Override
	public void activated(AuthorAccess authorAccess) {
		if (active) {
			initListeners(authorAccess.getEditorAccess().getEditorLocation());
			PluginWorkspaceProvider.getPluginWorkspace().addEditorChangeListener(editorChangeListener, PluginWorkspace.MAIN_EDITING_AREA);
			final WSEditor wsEditor = PluginWorkspaceProvider.getPluginWorkspace().getEditorAccess(authorAccess.getEditorAccess().getEditorLocation(), PluginWorkspace.MAIN_EDITING_AREA);
			if (wsEditor != null) {
				wsEditor.addEditorListener(editorListener);
			}
			DitaSemiaKeyRefEditListener keyRefEditListenerDocFilter	= new DitaSemiaKeyRefEditListener(authorAccess);
			authorAccess.getDocumentController().setDocumentFilter(keyRefEditListenerDocFilter);
			super.activated(authorAccess);
		}
	}	
	
	@Override
	public void deactivated(AuthorAccess authorAccess) {
		if (active) {
			super.deactivated(authorAccess);
			PluginWorkspaceProvider.getPluginWorkspace().removeEditorChangeListener(editorChangeListener, PluginWorkspace.MAIN_EDITING_AREA);
			final WSEditor wsEditor = PluginWorkspaceProvider.getPluginWorkspace().getEditorAccess(authorAccess.getEditorAccess().getEditorLocation(), PluginWorkspace.MAIN_EDITING_AREA);
			if (wsEditor != null) {
				wsEditor.removeEditorListener(editorListener);
			}
		}
	}
	
	private void initListeners(URL url) {
		if (active) {
			editorListener = new WSEditorListener() {
	
				@Override
				public void editorSaved(int arg0) {
					super.editorSaved(arg0);
					refreshCache(url);
				}
				
			};
			
			editorChangeListener = new WSEditorChangeListener() {
	
				@Override
				public void editorOpened(URL editorLocation) {
					super.editorOpened(editorLocation);
					if (isDitaFile(editorLocation)) {
						final BookCache bookCache 	= BookCacheHandler.getInstance().getBookCache(editorLocation);
						if (bookCache != null){
							final FileCache	fileCache	= bookCache.getFile(editorLocation);
							if (!fileCache.isUpdated()) {
								BookCacheHandler.getInstance().refreshFileCache(editorLocation);
							}
						}
						
					}
				}
	
				@Override
				public void editorClosed(URL editorLocation) {
					super.editorClosed(editorLocation);
					PluginWorkspaceProvider.getPluginWorkspace().removeEditorChangeListener(editorChangeListener, PluginWorkspace.MAIN_EDITING_AREA);
				}
				
			};
		}
	}
	
	private void refreshCache(URL url) {
		if (active) {
			if (isDitaFile(url)) {
				BookCacheHandler.getInstance().refreshFileCache(url);
			}
		}
	}
	
	private static boolean isDitaFile(URL url) {
		final String string = url.getFile();
		return (string.endsWith(".dita") || string.endsWith(".ditamap"));
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return active;
	}
}
