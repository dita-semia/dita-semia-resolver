package org.DitaSemia.Oxygen.AuthorOperations;

import java.awt.Component;
import java.awt.Frame;

import org.DitaSemia.Oxygen.DocumentCacheHandler;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefListInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyPrioritizer;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.AdvancedKeyRef.OxyAdvancedKeyrefDialog;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.node.NamespaceContext;

public class EditKeyRef implements AuthorOperation {

	private static final Logger logger = Logger.getLogger(EditKeyRef.class.getName());
	
	@Override
	public String getDescription() {
		return "Öffnet einen Dialog, um KeyRef-Elemente zu editieren.";
	}

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) {
		try {
			final int 						caretOffset 		= authorAccess.getEditorAccess().getCaretOffset();
			final AuthorDocumentController 	documentController 	= authorAccess.getDocumentController();
			final AuthorNode				nodeAtCaret 		= documentController.getNodeAtOffset(caretOffset);
			final KeyDefListInterface 		keyDefList 			= DocumentCacheHandler.getInstance().getDocumentCache(nodeAtCaret.getXMLBaseURL());
			final KeyRef 					contextKeyref 		= KeyRef.fromNode(new AuthorNodeWrapper(nodeAtCaret, authorAccess));
			// contextKeyDef?
			// ancestorKeyDef, keyrefFactory für keyPrioritizer?
			final KeyPrioritizer			keyPrioritizer		= new KeyPrioritizer(keyDefList, contextKeyref, null, null);
			final OxyAdvancedKeyrefDialog 	editKeyRefDialog 	= new OxyAdvancedKeyrefDialog((Frame)authorAccess.getWorkspaceAccess().getParentFrame(), keyDefList, contextKeyref, null, keyPrioritizer);

			editKeyRefDialog.setLocationRelativeTo((Component)authorAccess.getWorkspaceAccess().getParentFrame());

			KeyDefInterface keyDef;
			
			if (editKeyRefDialog.showDialog()) {
				keyDef = editKeyRefDialog.getSelectedKeyDef();
			} else {
				keyDef = null;
			}

			if (keyDef != null) {
				String keyValue = editKeyRefDialog.getKeyText();
				
				documentController.beginCompoundEdit();
				
				AuthorElement keyRefElement = (AuthorElement)nodeAtCaret;
				if (keyRefElement.getEndOffset() > keyRefElement.getStartOffset() + 1) {
					//logger.info("Lösche bestehenden Inhalt.");
					documentController.delete(keyRefElement.getStartOffset() + 1, keyRefElement.getEndOffset() - 1);
				}
				//logger.info("Setze Key = " + keyValue);
				documentController.insertText(keyRefElement.getStartOffset() + 1, keyValue);
				final NamespaceContext namespaceContext = nodeAtCaret.getNamespaceContext();
				String namespacePrefix	= namespaceContext.getPrefixForNamespace(KeyRef.NAMESPACE_URI);
				if (namespacePrefix == null) {
					namespacePrefix = KeyRef.NAMESPACE_PREFIX;
					documentController.setAttribute(
							"xmlns:" + KeyRef.NAMESPACE_PREFIX, 
							new AttrValue(KeyRef.NAMESPACE_URI), 
							documentController.getAuthorDocumentNode().getRootElement());
				}
		        documentController.setAttribute(namespacePrefix + ":" + KeyRef.ATTR_REF, new AttrValue(keyDef.getRefString()), keyRefElement);
		        
		        documentController.setAttribute(KeyRef.ATTR_OUTPUTCLASS, new AttrValue(editKeyRefDialog.getOutputclass()), keyRefElement);
				
				documentController.endCompoundEdit();
				
				authorAccess.getEditorAccess().refresh(nodeAtCaret);
			}
		} catch (Exception e) {
			logger.error(e, e);
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null;
	}

}
