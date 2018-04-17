package org.DitaSemia.Oxygen;

import java.net.URL;

import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.XsltConref.XsltConref;

import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.NamespaceContext;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;

public class OxyUtil {

	public static void setAttribute(
			AuthorDocumentController 	documentController,
			AuthorElement				element,
			String						namespaceUri,
			String						defaultNamespacePrefix,
			String						localName,
			String						value) {
		
		final NamespaceContext namespaceContext = element.getNamespaceContext();
		String namespacePrefix	= namespaceContext.getPrefixForNamespace(namespaceUri);
		if (namespacePrefix == null) {
			namespacePrefix = defaultNamespacePrefix;
			documentController.setAttribute(
					"xmlns:" + namespacePrefix, 
					new AttrValue(namespaceUri), 
					documentController.getAuthorDocumentNode().getRootElement());
		}
        documentController.setAttribute(namespacePrefix + ":" + localName, new AttrValue(value), element);
	}
	
	
	public static String getAuthorName() {
		WSEditor editorAccess = PluginWorkspaceProvider.getPluginWorkspace().getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
	    if (editorAccess != null) {
	    	WSEditorPage currentPage = editorAccess.getCurrentPage();
	    	if (currentPage instanceof WSAuthorEditorPage) {
	    		return ((WSAuthorEditorPage)currentPage).getAuthorAccess().getReviewController().getReviewerAuthorName();
	      }
	    }
		return null;
	}


	public static void ensureNamespacePrefix(
			AuthorDocumentController 	documentController,
			AuthorElement 				element, 
			String 						namespaceUri, 
			String 						namespacePrefix) {
		
		final NamespaceContext namespaceContext = element.getNamespaceContext();
		String checkNamespace	= namespaceContext.getNamespaceForPrefix(namespacePrefix);
		if ((checkNamespace == null) || (!checkNamespace.equals(namespaceUri))) {
			documentController.setAttribute(
					"xmlns:" + namespacePrefix, 
					new AttrValue(namespaceUri), 
					documentController.getAuthorDocumentNode().getRootElement());
		}
	}
	
}
