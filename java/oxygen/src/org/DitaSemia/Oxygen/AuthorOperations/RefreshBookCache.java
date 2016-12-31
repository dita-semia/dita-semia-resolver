package org.DitaSemia.Oxygen.AuthorOperations;

import org.DitaSemia.Oxygen.BookCacheHandler;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class RefreshBookCache implements AuthorOperation {

	private static final Logger logger = Logger.getLogger(RefreshBookCache.class.getName());

	@Override
	public String getDescription() {
		return "Cache aktualisieren";
	}

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) {
		try {
		
			final AuthorDocumentController 	documentController 	= authorAccess.getDocumentController();
			final AuthorNode				rootNode			= documentController.getNodeAtOffset(0);
			
			BookCacheHandler.getInstance().refreshBookCache(rootNode.getXMLBaseURL());

		} catch (Exception e) {
			logger.error(e, e);
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null;
	}

}
