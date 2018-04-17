package org.DitaSemia.Oxygen.AuthorOperations;

import java.awt.Component;
import java.awt.Frame;

import javax.swing.JOptionPane;

import org.DitaSemia.Oxygen.BookCacheHandler;
import org.DitaSemia.Oxygen.OxyUtil;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyPrioritizer;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.AdvancedKeyRef.OxyAdvancedKeyrefDialog;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class EditKeyRef implements AuthorOperation {

	private static final Logger logger = Logger.getLogger(EditKeyRef.class.getName());
	
	@Override
	public String getDescription() {
		return "Opens a dialog to edit key-xref elements.";
	}

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) {
		try {
			final int 						caretOffset 		= authorAccess.getEditorAccess().getCaretOffset();
			final AuthorDocumentController 	documentController 	= authorAccess.getDocumentController();
			final AuthorNode				nodeAtCaret 		= documentController.getNodeAtOffset(caretOffset);
			final BookCache					bookCache			= BookCacheHandler.getInstance().getBookCache(nodeAtCaret.getXMLBaseURL());
			
			if (bookCache == null) {
				JOptionPane.showMessageDialog((Frame)authorAccess.getWorkspaceAccess().getParentFrame(), "This operation can not be done while the BookCache is being refreshed. Please wait until the refresh is done and try again!", "Information", JOptionPane.INFORMATION_MESSAGE);
			} else {	
				//final KeyDefListInterface 		keyDefList 			= BookCacheHandler.getInstance().getBookCache(nodeAtCaret.getXMLBaseURL());
				final KeyRef 					keyRef 				= KeyRef.fromNode(new AuthorNodeWrapper(nodeAtCaret, authorAccess));
				final KeyDefInterface			contextKeyDef		= bookCache.getAncestorKeyDef(new AuthorNodeWrapper(nodeAtCaret, authorAccess), null);
				final KeyPrioritizer			keyPrioritizer		= new KeyPrioritizer(bookCache, keyRef, null, null);
			
				final OxyAdvancedKeyrefDialog 	editKeyRefDialog 	= new OxyAdvancedKeyrefDialog((Frame)authorAccess.getWorkspaceAccess().getParentFrame(), bookCache, keyRef, contextKeyDef, keyPrioritizer);
	
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
						documentController.delete(keyRefElement.getStartOffset() + 1, keyRefElement.getEndOffset() - 1);
					}
					OxyUtil.setAttribute(documentController, keyRefElement, KeyRef.NAMESPACE_URI, KeyRef.NAMESPACE_PREFIX, KeyRef.ATTR_REF, keyDef.getRefString());
			        
			        if (!keyRef.isOutputclassFixed()) {
			        	documentController.setAttribute(KeyRef.ATTR_OUTPUTCLASS, new AttrValue(editKeyRefDialog.getOutputclass()), keyRefElement);
			        }

			        // set text content as last step so the edit listener uses the new attributes
					documentController.insertText(keyRefElement.getStartOffset() + 1, keyValue);
					
					documentController.endCompoundEdit();
					
					authorAccess.getEditorAccess().refresh(nodeAtCaret);
				}
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
