package org.DitaSemia.Oxygen.XsltConref;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;

import org.DitaSemia.Base.XsltConref.XsltConref;
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
		return "Open the source file of current XSLT-Conref";
	}
	

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args) throws IllegalArgumentException, AuthorOperationException {
		try {
			final int 			caretOffset = authorAccess.getEditorAccess().getCaretOffset();
			final AuthorNode	nodeAtCaret = authorAccess.getDocumentController().getNodeAtOffset(caretOffset);
			//logger.info("nodeAtCaret: " + nodeAtCaret.getDisplayName());
			
			XsltConref	xsltConref 	= XsltConrefResolver.getInstance().xsltConrefFromNode(nodeAtCaret, authorAccess, true);
			if (xsltConref == null) {
				// check if the parent node is an XSLT-Conref
				xsltConref 	= XsltConrefResolver.getInstance().xsltConrefFromNode(nodeAtCaret.getParent(), authorAccess, true);
				//logger.info("nodeAtCaret.getParent(): " + nodeAtCaret.getParent().getDisplayName());
			}
			if (xsltConref != null) {
				final String 	filetype 	= (String)args.getArgumentValue(ARG_FILETYPE);
				final URL 		url 		= ((filetype != null) && (filetype.equalsIgnoreCase(FILETYPE_SOURCE))) ? xsltConref.getXmlSourceUrl() : xsltConref.getScriptUrl();
				//logger.info(xsltConref.getSourceType());
				if (xsltConref.getSourceType().equals(XsltConref.SOURCE_TYPE_EXCEL)) {
					Desktop desktop = Desktop.getDesktop();
					desktop.open(new File(url.toURI()));
				} else {
					PluginWorkspaceProvider.getPluginWorkspace().open(url, EditorPageConstants.PAGE_TEXT);
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
