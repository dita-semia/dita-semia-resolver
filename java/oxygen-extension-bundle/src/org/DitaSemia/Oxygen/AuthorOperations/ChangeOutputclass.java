package org.DitaSemia.Oxygen.AuthorOperations;

import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
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

public class ChangeOutputclass implements AuthorOperation {

	private static final Logger logger = Logger.getLogger(ChangeOutputclass.class.getName());
	
	@Override
	public String getDescription() {
		return "Changes the KeyRefs outputclass.";
	}

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap)
			throws IllegalArgumentException, AuthorOperationException {
		try {
			final int 						caretOffset 		= authorAccess.getEditorAccess().getCaretOffset();
			final AuthorDocumentController 	documentController 	= authorAccess.getDocumentController();
			final AuthorNode				nodeAtCaret 		= documentController.getNodeAtOffset(caretOffset);
			final KeyRef 					keyRef 				= KeyRef.fromNode(new AuthorNodeWrapper(nodeAtCaret, authorAccess));
			final AuthorElement 			keyRefElement 		= (AuthorElement)nodeAtCaret;
			
			if (keyRef != null) {
				String oc = keyRef.getOutputclass();
				documentController.beginCompoundEdit();
				switch (oc) {
				case KeyRef.OC_KEY:
					documentController.setAttribute(KeyRef.ATTR_OUTPUTCLASS, new AttrValue(KeyRef.OC_KEY_COLON_NAME), keyRefElement);
					break;
				case KeyRef.OC_KEY_COLON_NAME:
					documentController.setAttribute(KeyRef.ATTR_OUTPUTCLASS, new AttrValue(KeyRef.OC_KEY_DASH_NAME), keyRefElement);
					break;
				case KeyRef.OC_KEY_DASH_NAME:
					documentController.setAttribute(KeyRef.ATTR_OUTPUTCLASS, new AttrValue(KeyRef.OC_KEY_NAME_BRACED), keyRefElement);
					break;
				case KeyRef.OC_KEY_NAME_BRACED:
					documentController.setAttribute(KeyRef.ATTR_OUTPUTCLASS, new AttrValue(KeyRef.OC_NAME), keyRefElement);
					break;
				case KeyRef.OC_NAME:
					documentController.setAttribute(KeyRef.ATTR_OUTPUTCLASS, new AttrValue(KeyRef.OC_KEY), keyRefElement);
					break;
				}
				documentController.endCompoundEdit();
			}
		} catch (Exception e) {
			logger.error(e,e);
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		// TODO Auto-generated method stub
		return null;
	}

}
