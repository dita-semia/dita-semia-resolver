package org.DitaSemia.Oxygen.AdvancedKeyRef;


import org.DitaSemia.Base.BookCache;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Base.AdvancedKeyref.KeyTypeDef;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.DitaSemiaStylesFilter;
import org.apache.log4j.Logger;

import ro.sync.ecss.css.StaticContent;
import ro.sync.ecss.css.StringContent;
import ro.sync.ecss.css.Styles;
//import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.view.graphics.Font;
/*import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;*/

public class AdvancedKeyRefStylesFilter extends DitaSemiaStylesFilter {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AdvancedKeyRefStylesFilter.class.getName());
	

	private final static String	IMG_LINK		= "link.png";
	private final static String IMG_LINK_VALID 	= "link_valid.png";
	//private final static String IMG_LINK_EXTERN = "link_extern.png";
	private final static String IMG_LINK_BROKEN = "link_broken.png";
	
	private final static String 	CODE_FONT_NAME	= "Monospaced";
	private final static String[] 	CODE_FONT_NAMES	= {"Monospaced"};
	private final static double 	CODE_FONT_SCALE	= 0.9;

	private final static int	PSEUDO_LEVEL_LINK	= 2;
	private final static int	PSEUDO_LEVEL_PREFIX	= 1;
	private final static int	PSEUDO_LEVEL_SUFFIX	= 1;

	
	public static boolean filter(Styles styles, AuthorNode authorNode) {
		boolean handled = false;
		//logger.info("filter for node: " + authorNode.getName() + ", parent-node: " + (authorNode.getParent() == null ? "-" : authorNode.getParent().getName()) + ", pseudo-level: " + styles.getPseudoLevel());
		if (authorNode.getType() == AuthorNode.NODE_TYPE_PSEUDO_ELEMENT) {
			final boolean isBefore 	= (authorNode.getName().equals(BEFORE));
			final boolean isAfter 	= (!isBefore) && (authorNode.getName().equals(AFTER));
			
			if ((isBefore) && (styles.getPseudoLevel() == PSEUDO_LEVEL_LINK)) {
				handled = filterLink(styles, authorNode.getParent());
			} else if ((isBefore) && (styles.getPseudoLevel() == PSEUDO_LEVEL_PREFIX)) {
				handled = filterPrefix(styles, authorNode.getParent());
			} else if ((isAfter) && (styles.getPseudoLevel() == PSEUDO_LEVEL_SUFFIX)) {
				handled = filterSuffix(styles, authorNode.getParent());
			}
			
		} else if (authorNode.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
			handled = filterFont(styles, authorNode);
		}
		return handled;
	}
	

	private static KeyRef getKeyRefFromNode(AuthorNode authorNode) {
		return KeyRef.fromNode(new AuthorNodeWrapper(authorNode, null));
	}
	
	private static KeyTypeDef getKeyTypeDef(AuthorNode authorNode) {
		final BookCache cache 	= getBookCache(authorNode);
		if (cache != null) {
			String keyTypeName = null;
			final KeyRef keyRef = getKeyRefFromNode(authorNode);
			if (keyRef != null) {
				keyTypeName = keyRef.getType();
			} else {
				final NodeWrapper node = new AuthorNodeWrapper(authorNode, null);
				if (!KeyDef.nodeHasExplicitKey(node)) {
					keyTypeName = KeyDef.getTypeFromNode(node);
				}
			}
			if ((keyTypeName != null) && (!keyTypeName.isEmpty())) {
				return cache.getKeyTypeDef(keyTypeName);
			}
		}
		return null;
	}


	private static boolean filterLink(Styles styles, AuthorNode authorNode) {
		final KeyRef keyRef = getKeyRefFromNode(authorNode);
		if (keyRef != null) {
			final BookCache cache 	= getBookCache(authorNode);
			if (cache != null) {
				final KeyDefInterface	keyDef 	= cache.getExactMatch(keyRef);
				if (keyDef != null) {
					if (keyDef.getDefId() != null) {
						styles.setProperty(Styles.KEY_LINK, keyDef.getDefLocation());
						//logger.info("set link: " + keyDef.getDefLocation());
					}
					ChangeContentUri(styles, IMG_LINK, IMG_LINK_VALID);
				} else {
					ChangeContentUri(styles, IMG_LINK, IMG_LINK_BROKEN);
				}
				return true;
			}
		}
		return false;
	}
	
	private static boolean filterPrefix(Styles styles, AuthorNode authorNode) {
		final KeyTypeDef keyTypeDef = getKeyTypeDef(authorNode);
		if (keyTypeDef != null) {
			styles.setProperty(Styles.KEY_MIXED_CONTENT, new StaticContent[] { new StringContent(keyTypeDef.getPrefix()) });
			return true;
		} else {
			return false;
		}
	}
	
	private static boolean filterSuffix(Styles styles, AuthorNode authorNode) {
		final KeyTypeDef keyTypeDef = getKeyTypeDef(authorNode);
		if (keyTypeDef != null) {
			styles.setProperty(Styles.KEY_MIXED_CONTENT, new StaticContent[] { new StringContent(keyTypeDef.getSuffix()) });
			return true;
		} else {
			return false;
		}
	}
	
	private static boolean filterFont(Styles styles, AuthorNode authorNode) {
		final KeyTypeDef keyTypeDef = getKeyTypeDef(authorNode);
		if (keyTypeDef != null) {
			final Font font = styles.getFont();
			
			String 		fontName	= font.getName();
			int 		fontStyle 	= font.getStyle();
			int 		fontSize	= font.getSize();
			int			fontSpacing = font.getLetterSpacing();
			String[] 	fontNames	= font.getFontNames();
			
			if (keyTypeDef.isCodeFont()) {
				fontName 	= CODE_FONT_NAME;
				fontNames	= CODE_FONT_NAMES;
				fontSize	= (int)Math.round(fontSize * CODE_FONT_SCALE);
			}
			if (keyTypeDef.isItalicFont()) {
				fontStyle |= Font.ITALIC;
			}
			//logger.info("setProperty - fontName: " + fontName + ", fontStyle: " + fontStyle + ", fontSize: " + fontSize);
			styles.setProperty(Styles.KEY_FONT, new Font(fontName, fontStyle, fontSize, fontSpacing, fontNames));
			return true;
		} else {
			return false;
		}
	}

}
