/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Oxygen.XsltConref;

import org.DitaSemia.Base.XsltConref.XsltConref;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class RefreshXsltConref implements AuthorOperation {

	private static final Logger logger = Logger.getLogger(RefreshXsltConref.class.getName());

	@Override
	public String getDescription() {
		return "Refreshs the reference of an XSLT-Conref element.";
	}

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) throws IllegalArgumentException, AuthorOperationException {

		final AuthorEditorAccess 	editorAccess 	= authorAccess.getEditorAccess();
		final boolean 				isModified 		= editorAccess.isModified();
		
		try {
			final int 			caretOffset = authorAccess.getEditorAccess().getCaretOffset();
			final AuthorNode	nodeAtCaret = authorAccess.getDocumentController().getNodeAtOffset(caretOffset);
			
			AuthorNode 	xsltConrefNode 	= nodeAtCaret;
			XsltConref	xsltConref 	= XsltConrefResolver.getInstance().xsltConrefFromNode(xsltConrefNode, authorAccess, false);
			if (xsltConref == null) {
				// check if the parent node is an XSLT-Conref
				xsltConrefNode 	= nodeAtCaret.getParent();
				xsltConref 		= XsltConrefResolver.getInstance().xsltConrefFromNode(xsltConrefNode, authorAccess, false);
			}

			if (xsltConref != null) {
				// ensure the script will be recompiled.
				XsltConrefResolver.getInstance().getTransformerCache().removeFromCache(xsltConref.getScriptUrl());

				// ensure the schema will be reloaded.
				XsltConrefResolver.getInstance().getTransformerCache().getConfiguration().clearSchemaCache();
			}
			authorAccess.getDocumentController().refreshNodeReferences(xsltConrefNode);
			
		} catch (Exception e) {
			logger.error(e, e);
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
