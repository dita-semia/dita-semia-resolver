package org.DitaSemia.Oxygen.AuthorOperations;

import java.net.URL;

import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Oxygen.BookCacheHandler;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class RefreshFileCache implements AuthorOperation {

	private static final Logger logger = Logger.getLogger(RefreshFileCache.class.getName());
	
	@Override
	public String getDescription() {
		return "Refresh current file cache.";
	}

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap arg1) throws IllegalArgumentException, AuthorOperationException {
		try {
			
			final AuthorDocumentController 	documentController 	= authorAccess.getDocumentController();
			final AuthorNode				rootNode			= documentController.getNodeAtOffset(0);
			final URL						fileUrl				= rootNode.getXMLBaseURL();
	
			BookCacheHandler.getInstance().refreshFileCache(fileUrl);;

		} catch (Exception e) {
			logger.error(e, e);
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null;
	}

}
