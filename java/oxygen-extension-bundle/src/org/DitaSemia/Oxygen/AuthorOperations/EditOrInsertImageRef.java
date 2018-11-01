package org.DitaSemia.Oxygen.AuthorOperations;

import java.net.URL;

import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;

import ro.sync.ecss.dita.DITAAccess;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.XPathVersion;
import ro.sync.ecss.extensions.api.access.AuthorUtilAccess;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class EditOrInsertImageRef implements AuthorOperation {

	private static final Logger logger = Logger.getLogger(EditOrInsertImageRef.class.getName());

	private static final String ATTR_HREF 	= "href";
	private static final String TAG_IMAGE	= "image";
	
	@Override
	public String getDescription() {
		return "Opens a dialog to edit the reference of an image element.";
	}

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) throws IllegalArgumentException, AuthorOperationException {

		try {
			final int 			caretOffset 	= authorAccess.getEditorAccess().getCaretOffset();
			final AuthorNode 	selectedNode	= authorAccess.getEditorAccess().getFullySelectedNode();
			final AuthorNode 	nodeAtCaret		= authorAccess.getDocumentController().getNodeAtOffset(caretOffset);
			final AuthorNode 	nodeAfterCaret		= authorAccess.getDocumentController().getNodeAtOffset(caretOffset + 1);

			AuthorNode imageNode = null;
			if (isImageNode(selectedNode)) {
				imageNode = selectedNode;
			} else if(isImageNode(nodeAtCaret)) {
				imageNode = nodeAtCaret;
			} else if (isImageNode(nodeAfterCaret)) {
				imageNode = nodeAfterCaret;
			}

			if (imageNode != null) {
				final AuthorElement	element		= (AuthorElement)imageNode;
				final AttrValue		hrefAttr	= element.getAttribute(ATTR_HREF);
				final String 		imagePath 	= (hrefAttr == null) ? "" : hrefAttr.getValue(); 
				final String 		sel 		= chooseUrl(authorAccess, imagePath);
				if (sel != null) {
					authorAccess.getDocumentController().setAttribute(ATTR_HREF, new AttrValue(sel), element);
				}
			} else {
				final String sel = chooseUrl(authorAccess, ""); 
				if (sel != null) {
					// Prüfen, ob ein fig-Element eingefügt werden darf
					final Object[] result = authorAccess.getDocumentController().evaluateXPath(
							"oxy:allows-child-element(\"*\", \"class\", \"topic/fig\")",
							nodeAtCaret,	// contextNode
							false,			// ignoreTexts
							true, 			// ignoreCData
							true, 			// ignoreComments
							false,			// processChangeMarkers
							XPathVersion.XPATH_2_0);
					if ((result.length > 0) && ((Boolean)result[0])) {
						String figFragment = "<fig><title/><image href=\"" + sel + "\"/></fig>";
						authorAccess.getDocumentController().insertXMLFragment(figFragment, caretOffset);
					} else {
						DITAAccess.insertImage(authorAccess, sel);
					}
				}
			}
		} catch (BadLocationException e) {
			logger.error(e);
		}
	}
	
	protected static boolean isImageNode(AuthorNode node) {
		return (node != null) && (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) && (((AuthorElement)node).getLocalName().equals(TAG_IMAGE));
	}
	
	protected String chooseUrl(AuthorAccess authorAccess, String initialUrl) {
		final URL sel = authorAccess.getWorkspaceAccess().chooseURL(
				"Choose graphic file",
				new String[] { "gif", "jpg", "jpeg", "bmp", "png", "svg", "svgz", "wmf", "mathml", "mml", "cgm", "tif", "tiff", "eps", "ai"},
				"graphic files",
				initialUrl);
		if (sel != null) {
			AuthorUtilAccess util = authorAccess.getUtilAccess();
			return authorAccess.getXMLUtilAccess().escapeAttributeValue(
					util.makeRelative(authorAccess.getEditorAccess().getEditorLocation(), util.removeUserCredentials(sel)));
		} else {
			return null;
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null;
	}

}
