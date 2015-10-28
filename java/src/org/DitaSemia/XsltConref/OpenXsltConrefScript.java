package org.DitaSemia.XsltConref;

import java.net.URL;

import org.DitaSemia.JavaBase.AuthorNodeWrapper;
import org.DitaSemia.JavaBase.XslTransformerCache;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class OpenXsltConrefScript implements AuthorOperation {

	private static final Logger logger = Logger.getLogger(OpenXsltConrefScript.class.getName());

	@Override
	public String getDescription() {
		return "Opens the script of current XSLT-Conref";
	}

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args) throws IllegalArgumentException, AuthorOperationException {
		try {
			final int 			caretOffset = authorAccess.getEditorAccess().getCaretOffset();
			final AuthorNode	nodeAtCaret = authorAccess.getDocumentController().getNodeAtOffset(caretOffset);
			
			final XsltConref 	xsltConref	= XsltConref.fromNode(new AuthorNodeWrapper(nodeAtCaret, authorAccess));
			if (xsltConref != null) {
				PluginWorkspaceProvider.getPluginWorkspace().open(xsltConref.getScriptUrl(), EditorPageConstants.PAGE_TEXT);
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
