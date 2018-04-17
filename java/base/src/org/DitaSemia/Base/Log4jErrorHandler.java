package org.DitaSemia.Base;

import org.apache.log4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class Log4jErrorHandler implements ErrorHandler {
	
	private Logger logger;
	
	public Log4jErrorHandler(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		logger.warn(exception.getMessage());
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		logger.error(exception.getMessage());
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		logger.error(exception.getMessage());	// don't log as fatal error since this would be displayed in an oXygen popup dialog.
	}
}