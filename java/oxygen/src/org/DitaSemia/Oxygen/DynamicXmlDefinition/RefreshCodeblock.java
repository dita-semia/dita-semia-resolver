package org.DitaSemia.Oxygen.DynamicXmlDefinition;

import java.awt.Frame;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class RefreshCodeblock implements AuthorOperation {


	private static final Logger logger = Logger.getLogger(RefreshCodeblock.class.getName());

	@Override
	public String getDescription() {
		return "Refresh DXD codeblock.";
	}

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) {

		final AuthorEditorAccess 		editorAccess 		= authorAccess.getEditorAccess();
		final boolean 					isModified 			= editorAccess.isModified();
		final AuthorDocumentController	documentController	= authorAccess.getDocumentController();
		
		try {
			final int 			caretOffset = editorAccess.getCaretOffset();
			final AuthorNode	nodeAtCaret = documentController.getNodeAtOffset(caretOffset);
			
			AuthorNode 	dxdCodeblockNode 	= nodeAtCaret;
			if (!OxygenDxdCodeblockResolver.isDxdCodeblock(dxdCodeblockNode)) {
				// check if the parent node is a DXD codeblock
				dxdCodeblockNode 	= nodeAtCaret.getParent();
				if (!OxygenDxdCodeblockResolver.isDxdCodeblock(dxdCodeblockNode)) { 
					dxdCodeblockNode = null;
				}
			}

			if (dxdCodeblockNode != null) {
				// ensure the import script will be recompiled.
				OxygenDxdCodeblockResolver.getInstance().removeScripFromCache(dxdCodeblockNode);
			}
			documentController.refreshNodeReferences(dxdCodeblockNode);
			
		} catch (Exception e) {
			logger.error(e, e);
			
            JOptionPane.showMessageDialog(
            		(Frame)PluginWorkspaceProvider.getPluginWorkspace().getParentFrame(),
            		e.getMessage(),
            	    "Refresh DXD codeblock",
            	    JOptionPane.ERROR_MESSAGE);
		}

		if (!isModified) {
			editorAccess.setModified(false);
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null;
	}
}
