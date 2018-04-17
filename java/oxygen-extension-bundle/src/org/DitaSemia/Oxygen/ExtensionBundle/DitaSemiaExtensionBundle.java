/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Oxygen.ExtensionBundle;

import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.BookCacheHandler;
import org.DitaSemia.Oxygen.AuthorOperations.ProfilingOptionsDialog;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;
import ro.sync.ecss.extensions.api.AuthorReferenceResolver;
import ro.sync.ecss.extensions.api.StylesFilter;
import ro.sync.ecss.extensions.api.UniqueAttributesRecognizer;
import ro.sync.ecss.extensions.api.content.ClipboardFragmentProcessor;
import ro.sync.ecss.extensions.api.link.LinkTextResolver;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.dita.DITAExtensionsBundle;

public class DitaSemiaExtensionBundle extends DITAExtensionsBundle {

	private static final String KEY_TYPE_DEF_LIST_URL			= "KeyTypeDefList.xml";
	
	private boolean active 		= true;
	
	private static ProfilingOptionsDialog dialog = null;
	//private static boolean dialogInit 	= false;
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DitaSemiaExtensionBundle.class.getName());
	
	
	public static void initDocumentCacheHandler() {
		final BookCacheHandler cacheHandler = BookCacheHandler.getInstance();
		if (cacheHandler.getGlobalKeyTypeDefUrl() == null) {
			cacheHandler.setGlobalKeyTypeDefUrl(DitaSemiaExtensionBundle.class.getClassLoader().getResource(KEY_TYPE_DEF_LIST_URL));
		}
	}
	
	
	public DitaSemiaExtensionBundle() {
		initDocumentCacheHandler();
		getDialog().setExtensionBundle(this);
	}
	
	@Override
	public String getDescription() {
		return "DITA-SEMIA extension bundle";
	}
	
	@Override
	public boolean isContentReference(AuthorNode node) {
		if (active) {
			boolean isContentReference = false;
			if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null), false)) {
				isContentReference = true;
			} else {
				isContentReference = super.isContentReference(node);
			}
			//logger.info("isContentReference(" + node.getDisplayName() + "): " + isContentReference);
			return isContentReference;
		} else {
			return false;
		}

	}
	
	
	@Override
	public AuthorReferenceResolver createAuthorReferenceResolver() {
		DitaSemiaReferenceResolver resolver = new DitaSemiaReferenceResolver();
		dialog.setReferenceResolver(resolver);
		return resolver;
	}
	
	@Override
	public LinkTextResolver createLinkTextResolver() {
		DitaSemiaLinkTextResolver resolver = new DitaSemiaLinkTextResolver();
		dialog.setLinkTextResolver(resolver);
		return resolver;
	}
	
	
	@Override
	public StylesFilter createAuthorStylesFilter() {
		DitaSemiaStylesFilter filter = new DitaSemiaStylesFilter();
		dialog.setStylesFilter(filter);
		return filter;
	}

	private DitaSemiaUniqueAttributesRecognizer uniqueAttributesRecognizer;

	@Override
	public AuthorExtensionStateListener createAuthorExtensionStateListener() {
		uniqueAttributesRecognizer = new DitaSemiaUniqueAttributesRecognizer();
		dialog.setUniqueAttributesRecognizer(uniqueAttributesRecognizer);
		return uniqueAttributesRecognizer;
	}

	@Override
	public ClipboardFragmentProcessor getClipboardFragmentProcessor() {
		return uniqueAttributesRecognizer;
	}
	
	@Override
	public UniqueAttributesRecognizer getUniqueAttributesIdentifier() {
		return uniqueAttributesRecognizer;
	}
	
	public static ProfilingOptionsDialog getDialog() {
		if (dialog == null) {
			dialog = new ProfilingOptionsDialog();
		} 
		return dialog;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return active;
	}
}
