package org.DitaSemia.Oxygen.Conbat;

import org.DitaSemia.Base.EmbeddedXPathResolver;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.apache.log4j.Logger;

import net.sf.saxon.trans.XPathException;
import ro.sync.ecss.css.LabelContent;
import ro.sync.ecss.css.StaticContent;
import ro.sync.ecss.css.Styles;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;

public class ConbatStylesFilter {

	private static final Logger logger = Logger.getLogger(ConbatStylesFilter.class.getName());
	
	public static final String	ERR_TEXT				="<ERR>";
	public static final String 	LABEL_CONTENT_TEXT_KEY 	= "text";

	public static boolean filter(Styles styles, AuthorNode authorNode) {
		boolean handled = false;
		final StaticContent[] mixedContent = styles.getMixedContent();
		if ((authorNode.getType() == AuthorNode.NODE_TYPE_ELEMENT) || (authorNode.getType() == AuthorNode.NODE_TYPE_PSEUDO_ELEMENT)) {
			logger.info(authorNode.getName() + ": " + styles.getMixedContent());
			logger.info(styles.getMixedContent().length);
			if (mixedContent.length > 0) {
				logger.info(styles.getMixedContent()[0].getClass());
				logger.info(styles.getPseudoLevel());
			}
			if ((mixedContent.length == 1) && (mixedContent[0] instanceof LabelContent)) {
				handled = true;
				final LabelContent 	content = (LabelContent)mixedContent[0];
				final String 		text 	= (String)content.getProperties().get(LABEL_CONTENT_TEXT_KEY);
				if (text.contains("{")) {
					
					final AuthorNode 		contextNode = (styles.getPseudoLevel() == 0) ? authorNode : authorNode.getParent();
					final AuthorNodeWrapper	context		= new AuthorNodeWrapper(contextNode, getAuthorAccess());
					try {
						final String resolved	= EmbeddedXPathResolver.resolve(text, context);
						content.getProperties().put(LABEL_CONTENT_TEXT_KEY, resolved);
					} catch (XPathException e) {
						content.getProperties().put(LABEL_CONTENT_TEXT_KEY, ERR_TEXT);
						logger.error(e, e);
					}
				}
			}
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
