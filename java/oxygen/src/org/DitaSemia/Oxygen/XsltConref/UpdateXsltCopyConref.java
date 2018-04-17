package org.DitaSemia.Oxygen.XsltConref;

import java.awt.Frame;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.apache.log4j.Logger;

import net.sf.saxon.trans.XPathException;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class UpdateXsltCopyConref implements AuthorOperation {

	private static final Logger logger = Logger.getLogger(UpdateXsltCopyConref.class.getName());
	
	@Override
	public String getDescription() {
		return "Updates the copy of an XSLT-Conref element.";
	}

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap arguments) throws IllegalArgumentException, AuthorOperationException {
		try {
			final int 				caretOffset 	= authorAccess.getEditorAccess().getCaretOffset();
			final AuthorNode		node 			= authorAccess.getDocumentController().getNodeAtOffset(caretOffset);
			final AuthorNodeWrapper xsltConrefNode	= new AuthorNodeWrapper(node, authorAccess);
			final XsltConref		xsltConref 		= XsltConrefResolver.getInstance().xsltConrefFromNode(node, authorAccess, true);
				
			if (xsltConref != null) {
				updateXsltCopyConref(xsltConref, xsltConrefNode, authorAccess);
			}
			
		} catch (Exception e) {
			
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null;
	}

	public static void updateXsltCopyConref(XsltConref xsltConref, AuthorNodeWrapper xsltConrefNode, AuthorAccess authorAccess) {
		final AuthorDocumentController documentController = authorAccess.getDocumentController();
		documentController.beginCompoundEdit();
		final AuthorElement oldElement = (AuthorElement)xsltConrefNode.getAuthorNode();
		
		try {
			// keep all attributes of currenold element
			final Map<String, String> attributes = new HashMap<>();
			for (int i = 0; i < oldElement.getAttributesCount(); ++i) {
				final String 	attrName 	= oldElement.getAttributeAtIndex(i);
				final AttrValue attrValue	= oldElement.getAttribute(attrName);
				if (attrValue.isSpecified()) {
					attributes.put(attrName, attrValue.getValue());
				}
			}

			if (xsltConref != null) {
				// ensure the script will be recompiled.
				XsltConrefResolver.getInstance().getTransformerCache().removeFromCache(xsltConref.getScriptUrl());

				// ensure the schema will be reloaded.
				XsltConrefResolver.getInstance().getTransformerCache().getConfiguration().clearSchemaCache();
			}
			
			final String resolved = xsltConref.resolveToString(null);
			
			if ((resolved == null) || (resolved.isEmpty())) {
				throw new XPathException("The script did not return any content.");
			}
			
			//logger.info("resolved:" + resolved);
			
			int startOffset = oldElement.getStartOffset();
			int endOffset 	= oldElement.getEndOffset();
			
			documentController.delete(startOffset, endOffset);
			final AuthorDocumentFragment 	resolvedFragment 	= documentController.createNewDocumentFragmentInContext(resolved, startOffset);
			final List<AuthorNode>			resolvedNodes		= resolvedFragment.getContentNodes();
			
			AuthorElement resolvedElement = null;
			for (AuthorNode node: resolvedNodes) {
				if (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
					resolvedElement = (AuthorElement)node;
					break;
				}
			}
			
			// remove all processing instructions and other non-element content before the actual content
			/*while (documentController.getNodeAtOffset(startOffset + 1).getType() != AuthorNode.NODE_TYPE_ELEMENT) {
				documentController.deleteNode(documentController.getNodeAtOffset(startOffset + 1));
			}*/
			//final AuthorElement newElement = (AuthorElement)documentController.getNodeAtOffset(startOffset + 1);
			
			// remove reparse-Element (not relevant for copy-conref)
			resolvedElement.removeAttribute(XsltConref.NAMESPACE_PREFIX + ":" + XsltConref.FLAG_REPARSE);
			
			// change name to original one
			resolvedElement.setName(oldElement.getName());
			
			// insert all attributes from old element that are not already present in new element
			for (Entry<String, String> entry : attributes.entrySet()) {
				//logger.info("attribute: " + entry.getKey() + " exists: " + (resolvedElement.getAttribute(entry.getKey()) != null));
				final AttrValue existingAttr = resolvedElement.getAttribute(entry.getKey());
				if ((existingAttr == null) || (!existingAttr.isSpecified())) {
					resolvedElement.setAttribute(entry.getKey(), new AttrValue(entry.getValue()));
				}
			}
			documentController.insertFragmentSchemaAware(startOffset, resolvedFragment);
			
		} catch (Exception e) {
			logger.error(e, e);
			JOptionPane.showMessageDialog(
					(Frame)authorAccess.getWorkspaceAccess().getParentFrame(),
					"Updateing the XSLT-copy-Conref failed:\n" + e.getMessage(), 
					"ERROR",  
					JOptionPane.ERROR_MESSAGE);

			//documentController.delete(oldElement.getStartOffset() + 1, oldElement.getEndOffset() - 1);
		}
		documentController.endCompoundEdit();
	}
	
}
