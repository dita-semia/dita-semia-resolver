package org.DitaSemia.XsltConref;

import java.net.URL;

import org.DitaSemia.JavaBase.AuthorNodeWrapper;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class OpenXsltConrefFile implements AuthorOperation {

	private static final Logger logger = Logger.getLogger(OpenXsltConrefFile.class.getName());
	
	private static final String	ARG_FILETYPE	= "filetype";
	@SuppressWarnings("unused")
	private static final String	FILETYPE_SCRIPT	= "script";
	private static final String	FILETYPE_SOURCE	= "source";

	@Override
	public String getDescription() {
		return "Opens a file for the of current XSLT-Conref";
	}
	

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args) throws IllegalArgumentException, AuthorOperationException {
		try {
			final int 			caretOffset = authorAccess.getEditorAccess().getCaretOffset();
			final AuthorNode	nodeAtCaret = authorAccess.getDocumentController().getNodeAtOffset(caretOffset);
			//logger.info("nodeAtCaret: " + nodeAtCaret.getDisplayName());
			
			XsltConref 	xsltConref	= XsltConref.fromNode(new AuthorNodeWrapper(nodeAtCaret, authorAccess));
			if (xsltConref == null) {
				// check if the parent node is an XSLT-Conref
				xsltConref	= XsltConref.fromNode(new AuthorNodeWrapper(nodeAtCaret.getParent(), authorAccess));
				//logger.info("nodeAtCaret.getParent(): " + nodeAtCaret.getParent().getDisplayName());
			}
			if (xsltConref != null) {
				final String 	filetype 	= (String)args.getArgumentValue(ARG_FILETYPE);
				//logger.info("filetype: " + filetype);
				final URL 		url 		= ((filetype != null) && (filetype.equalsIgnoreCase(FILETYPE_SOURCE))) ? xsltConref.getXmlSourceUrl() : xsltConref.getScriptUrl();
				PluginWorkspaceProvider.getPluginWorkspace().open(url, EditorPageConstants.PAGE_TEXT);
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
