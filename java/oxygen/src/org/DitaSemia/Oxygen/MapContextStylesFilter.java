package org.DitaSemia.Oxygen;

import org.DitaSemia.Base.DocumentCaching.FileCache;
import org.apache.log4j.Logger;

import ro.sync.ecss.css.StaticContent;
import ro.sync.ecss.css.Styles;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class MapContextStylesFilter extends DitaSemiaStylesFilter {


	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MapContextStylesFilter.class.getName());

	private final static int	PSEUDO_LEVEL_ROOT	= 102;
	private final static int	PSEUDO_LEVEL_PARENT	= 101;
	
	private final static int	INDEX_TITLE			= 1;
	private final static int	INDEX_TAG			= 2;
	private final static int	INDEX_URL			= 3;
	private final static int	INDEX_MAX			= 3;

	public static boolean filter(Styles styles, AuthorNode authorNode) {
		boolean handled = false;
		//logger.info("filter for node: " + authorNode.getName() + ", parent-node: " + (authorNode.getParent() == null ? "-" : authorNode.getParent().getName()) + ", pseudo-level: " + styles.getPseudoLevel());
		if (authorNode.getType() == AuthorNode.NODE_TYPE_PSEUDO_ELEMENT) {

			final boolean isBefore 	= (authorNode.getName().equals(BEFORE));
			
			if ((isBefore) && (styles.getPseudoLevel() == PSEUDO_LEVEL_ROOT)) {
				handled = filterMapContext(styles, getBookCache(authorNode).getRootFile());
			} else if ((isBefore) && (styles.getPseudoLevel() == PSEUDO_LEVEL_PARENT)) {
				handled = filterMapContext(styles, getBookCache(authorNode).getParentFile(authorNode.getXMLBaseURL()));
			}
		}
		return handled;
	}

	private static boolean filterMapContext(Styles styles, FileCache file) {
		boolean handled = false;
		StaticContent[] content = styles.getMixedContent();
		if ((content != null) && (content.length > INDEX_MAX)) {
			if (file != null) {
				final String title = file.getLinkText(null, null);
				if (title != null) {
					setLabelText(content[INDEX_TITLE], 	title);
				} else {
					setLabelText(content[INDEX_TITLE], 	"");
				}
				setLabelText(content[INDEX_TAG], 	file.getRootNode().getName());
				setLabelText(content[INDEX_URL], 	file.getDecodedUrl());
				styles.setProperty(Styles.KEY_LINK, file.getDecodedUrl());
			} else {
				styles.setProperty(Styles.KEY_MIXED_CONTENT, 	null);	// don't display anything
				styles.setProperty(Styles.KEY_DISPLAY, 			DISPLAY_NONE);
			}
			handled = true;
		}
		return handled;
	}
}
