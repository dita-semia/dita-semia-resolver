package org.DitaSemia.Oxygen.AuthorOperations;

import org.DitaSemia.Oxygen.BookCacheHandler;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;

public class ClearAllCaches implements AuthorOperation {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ClearAllCaches.class.getName());

	@Override
	public String getDescription() {
		return "Cache aktualisieren";
	}

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) {
		BookCacheHandler.getInstance().clearCache();
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null;
	}

}
