package org.DitaSemia.Oxygen.AuthorOperations;

import java.awt.Component;
import java.awt.Frame;

import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInformationDialog;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class ShowKeyDefInformation implements AuthorOperation {

	private static final Logger logger = Logger.getLogger(ShowKeyDefInformation.class.getName());
	
	@Override
	public String getDescription() {
		return "Öffnet einen Dialog, um die Informationen über den definierten Key anzuzeigen.";
	}

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) {
		try {
			final int 						caretOffset 		= authorAccess.getEditorAccess().getCaretOffset();
			final AuthorDocumentController 	documentController 	= authorAccess.getDocumentController();
			final AuthorNode				nodeAtCaret 		= documentController.getNodeAtOffset(caretOffset);
			final AuthorNodeWrapper			authorNode			= new AuthorNodeWrapper(nodeAtCaret, authorAccess);

			KeyDef keyDef = KeyDef.fromNode(authorNode);
			KeyDefInformationDialog keyInformationDialog = new KeyDefInformationDialog((Frame)authorAccess.getWorkspaceAccess().getParentFrame(), keyDef);

			keyInformationDialog.setLocationRelativeTo((Component)authorAccess.getWorkspaceAccess().getParentFrame());

			keyInformationDialog.showDialog();
			
		} catch (Exception e) {
			logger.error(e, e);
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null;
	}

}
