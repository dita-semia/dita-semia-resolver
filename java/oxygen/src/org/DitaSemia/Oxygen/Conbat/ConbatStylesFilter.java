package org.DitaSemia.Oxygen.Conbat;

import org.DitaSemia.Base.EmbeddedXPathResolver;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.DitaSemiaStylesFilter;
import org.apache.log4j.Logger;

import net.sf.saxon.trans.XPathException;
import ro.sync.ecss.css.StaticContent;
import ro.sync.ecss.css.StringContent;
import ro.sync.ecss.css.Styles;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.view.graphics.Color;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;

public class ConbatStylesFilter extends DitaSemiaStylesFilter {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ConbatStylesFilter.class.getName());
	
	public static final String 	NAMESPACE_URI			= "http://www.dita-semia.org/conbat";
	
	public static final String	ERR_PREFIX				="[ERR: ";
	public static final String	ERR_SUFFIX				="]";
	public static final String	ERR_MSG_REMOVE_REGEXP	= "^.*XPath failed due to: ";
	//public static final String 	LABEL_CONTENT_TEXT_KEY 	= "text";
	
	public static final int		PSEUDO_LEVEL_INLINE		= 14;
	public static final int		PSEUDO_LEVEL_PARAGRAPH	= 10;
	//public static final int		PSEUDO_LEVEL_DEFAULT	= 11;	// handled by ConbatContentResovler using oxy_link-text()
	public static final int		PSEUDO_LEVEL_TITLE		= 15;
	//public static final int		PSEUDO_LEVEL_HEADER		= 16;	not handled yet
	//public static final int		PSEUDO_LEVEL_DT			= 17;	not handled yet

	public static final String 	ATTR_CONTENT			= "content";
	public static final String 	ATTR_DEFAULT_CONTENT	= "default-content";
	public static final String	ATTR_PREFIX				= "prefix";
	public static final String	ATTR_SUFFIX				= "suffix";
	public static final String	ATTR_TITLE				= "title";

	public static boolean filter(Styles styles, AuthorNode authorNode) {
		boolean handled = false;
		if (authorNode.getType() == AuthorNode.NODE_TYPE_PSEUDO_ELEMENT) {
			final int pseudoLevel = styles.getPseudoLevel();
			if ((pseudoLevel == PSEUDO_LEVEL_INLINE) || (pseudoLevel == PSEUDO_LEVEL_PARAGRAPH)) {
				if (authorNode.getName().equals(BEFORE)) {
					handled = resolve(styles, authorNode, ATTR_PREFIX);
				} else if (authorNode.getName().equals(AFTER)) { 
					handled = resolve(styles, authorNode, ATTR_SUFFIX);
				}
			} else if (pseudoLevel == PSEUDO_LEVEL_TITLE) {
				if (authorNode.getName().equals(BEFORE)) {
					handled = resolve(styles, authorNode, ATTR_TITLE);
				}
			}
		}
		return handled;
	}
	
	private static boolean resolve(Styles styles, AuthorNode authorNode, String attrName) {
		boolean handled = false;
		final StaticContent[] mixedContent = styles.getMixedContent();
		if ((mixedContent != null) && (mixedContent.length > 0)) {
			final AuthorAccess authorAccess = getAuthorAccess();
			if (authorAccess != null) {
				final AuthorNodeWrapper context 	= new AuthorNodeWrapper(authorNode.getParent(), authorAccess);
				final String			attrValue	= context.getAttribute(attrName, NAMESPACE_URI);
				if (attrValue != null) {
					String resolved;
					try {
						resolved = EmbeddedXPathResolver.resolve(attrValue, context);
					} catch (XPathException e) {
						final String errMsg = e.getMessage().replaceFirst(ERR_MSG_REMOVE_REGEXP, "");
						resolved = ERR_PREFIX + errMsg + ERR_SUFFIX;
						styles.setProperty(Styles.KEY_FONT_WEIGHT, Styles.FONT_WEIGHT_BOLD);
						styles.setProperty(Styles.KEY_FOREGROUND_COLOR, Color.COLOR_RED);
					}
					styles.getMixedContent()[0] = new StringContent(resolved);
				}
			}
			handled = true;
		}
		return handled;
	}
	
	private static AuthorAccess getAuthorAccess() {
		WSEditor editorAccess = PluginWorkspaceProvider.getPluginWorkspace().getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
	    if (editorAccess != null) {
	      WSEditorPage currentPage = editorAccess.getCurrentPage();
	      if (currentPage instanceof WSAuthorEditorPage) {
	        return ((WSAuthorEditorPage)currentPage).getAuthorAccess();
	      }
	    }
		return null;
	}
}
