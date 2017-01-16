package org.DitaSemia.Oxygen.Conbat;

import javax.swing.text.BadLocationException;

import org.DitaSemia.Base.EmbeddedXPathResolver;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.DitaSemiaStylesFilter;
import org.apache.log4j.Logger;

import net.sf.saxon.trans.XPathException;
import ro.sync.ecss.css.StaticContent;
import ro.sync.ecss.css.StringContent;
import ro.sync.ecss.css.Styles;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.view.graphics.Color;
import ro.sync.exml.view.graphics.Font;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;

public class ConbatStylesFilter extends DitaSemiaStylesFilter {

	private static final Logger logger = Logger.getLogger(ConbatStylesFilter.class.getName());
	
	public static final String 	NAMESPACE_URI			= "http://www.dita-semia.org/conbat";
	
	public static final String	ERR_PREFIX				="[ERR: ";
	public static final String	ERR_SUFFIX				="]";
	public static final String	ERR_MSG_REMOVE_REGEXP	= "^.*XPath failed due to: ";
	
	public static final int		PSEUDO_LEVEL_INLINE		= 5;
	public static final int		PSEUDO_LEVEL_EDIT_MIN	= 6;
	public static final int		PSEUDO_LEVEL_EDIT_MAX	= 9;
	public static final int		PSEUDO_LEVEL_PARAGRAPH	= 10;
	public static final int		PSEUDO_LEVEL_DEFAULT	= 11;
	public static final int		PSEUDO_LEVEL_TITLE		= 15;
	//public static final int		PSEUDO_LEVEL_HEADER		= 16;	not handled yet
	//public static final int		PSEUDO_LEVEL_DT			= 17;	not handled yet

	public static final String 	ATTR_CONTENT			= "content";
	public static final String 	ATTR_DEFAULT_CONTENT	= "default-content";
	public static final String	ATTR_PREFIX				= "prefix";
	public static final String	ATTR_SUFFIX				= "suffix";
	public static final String	ATTR_TITLE				= "title";
	public static final String	ATTR_POPUP_EDIT			= "popup-edit";
	public static final String	ATTR_PE_HIDE_VALUE		= "pe-hide-value";
	
	public static final String	VALUE_TRUE				= "true";
	public static final String	POPUP_EDIT_TEXT			= "#text";
	
	public static final Color	EDIT_ONLY_COLOR			= new Color(140, 140, 140);
	public static final double	EDIT_ONLY_FONT_SCALE	= 0.8;

	public static boolean filter(Styles styles, AuthorNode authorNode) {
		boolean handled = false;
		//logger.info("filter for node: " + authorNode.getName() + ", parent-node: " + (authorNode.getParent() == null ? "-" : authorNode.getParent().getName()) + ", pseudo-level: " + styles.getPseudoLevel());
		final int nodeType = authorNode.getType();
		if (nodeType == AuthorNode.NODE_TYPE_PSEUDO_ELEMENT) {
			final int pseudoLevel = styles.getPseudoLevel();
			//logger.info("filter: " + authorNode.getType() + ", " + authorNode.getName() + ", " + pseudoLevel);
			if ((pseudoLevel == PSEUDO_LEVEL_INLINE) || (pseudoLevel == PSEUDO_LEVEL_PARAGRAPH)) {
				if (authorNode.getName().equals(BEFORE)) {
					handled = resolve(styles, authorNode.getParent(), ATTR_PREFIX, true);
				} else if (authorNode.getName().equals(AFTER)) { 
					handled = resolve(styles, authorNode.getParent(), ATTR_SUFFIX, true);
				}
			} else if (pseudoLevel == PSEUDO_LEVEL_TITLE) {
				if (authorNode.getName().equals(BEFORE)) {
					handled = resolve(styles, authorNode.getParent(), ATTR_TITLE, false);
				}
			} else if (pseudoLevel == PSEUDO_LEVEL_DEFAULT) {
				final AuthorNode parentNode = authorNode.getParent();
				if (parentNode.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
					final AuthorElement parent = (AuthorElement)parentNode;
					try {
						if ((parent.getContentNodes().isEmpty()) && (parent.getTextContent().isEmpty())) {
							handled = resolve(styles, authorNode.getParent(), ATTR_DEFAULT_CONTENT, true);
						}
					} catch (BadLocationException e) {
						logger.error(e, e);
					}
				}
			} else if ((pseudoLevel >= PSEUDO_LEVEL_EDIT_MIN) && (pseudoLevel <= PSEUDO_LEVEL_EDIT_MAX)) {
				handled = filterEdit(styles, authorNode.getParent());
			}
		} else if (nodeType == AuthorNode.NODE_TYPE_ELEMENT) {
			handled = resolve(styles, authorNode, ATTR_CONTENT, true);
			final AttrValue popupEdit = ((AuthorElement)authorNode).getAttribute(ATTR_POPUP_EDIT);
			if ((popupEdit != null) && (POPUP_EDIT_TEXT.equals(popupEdit.getValue()))) {
				handled = filterEdit(styles, authorNode);
			}
		}
		return handled;
	}
	
	private static boolean resolve(Styles styles, AuthorNode contextNode, String attrName, boolean replaceFirst) {
		boolean handled = false;
		final StaticContent[] mixedContent = styles.getMixedContent();
		if ((mixedContent != null) && (mixedContent.length > 0)) {
			final AuthorAccess authorAccess = getAuthorAccess();
			if (authorAccess != null) {
				final AuthorNodeWrapper context 	= new AuthorNodeWrapper(contextNode, authorAccess);
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
					styles.getMixedContent()[replaceFirst ? 0 : styles.getMixedContent().length-1] = new StringContent(resolved);
				}
			}
			handled = true;
		}
		return handled;
	}
	
	private static boolean filterEdit(Styles styles, AuthorNode contextNode) {
		boolean handled = false;
		final AuthorNodeWrapper node = new AuthorNodeWrapper(contextNode, null);
		final String popupEdit = node.getAttribute(ATTR_POPUP_EDIT, 	NAMESPACE_URI);
		if ((popupEdit != null) && (!popupEdit.isEmpty())) {
			final String hideValue = node.getAttribute(ATTR_PE_HIDE_VALUE, 	NAMESPACE_URI);
			if ((hideValue != null) && (!hideValue.isEmpty())) {	
				final String value = (popupEdit.equals(POPUP_EDIT_TEXT)) ? node.getTextContent() : node.getAttribute(popupEdit, null);
				if (hideValue.equals(value)) {
					styles.setProperty(Styles.KEY_FOREGROUND_COLOR, EDIT_ONLY_COLOR);
					final Font originalFont = styles.getFont();
					final Font newFont		= new Font(
							originalFont.getName(), 
							originalFont.getStyle(), 
							(int)(originalFont.getSize() * EDIT_ONLY_FONT_SCALE),
							originalFont.getLetterSpacing(),
							originalFont.getFontNames());
					styles.setProperty(Styles.KEY_FONT, newFont);
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
