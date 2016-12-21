package org.DitaSemia.Base;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import net.sf.saxon.lib.UnfailingErrorListener;

public class Log4jErrorListener implements UnfailingErrorListener {
	
	private final Logger logger;
	
	public Log4jErrorListener(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void warning(TransformerException exception) {
		logger.warn(exception.getMessage());
	}

	@Override
	public void error(TransformerException exception) {
		logger.error(exception.getMessage());
	}

	@Override
	public void fatalError(TransformerException exception) {
		// don't use logger.fatal since this message would be displayed in oxygen as well.
		logger.error("FATAL: " + exception.getMessage());
	}
}