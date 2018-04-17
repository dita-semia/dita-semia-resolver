/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Oxygen.ExtensionBundle;

import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.AuthorOperations.ProfilingOptionsDialog;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;
import ro.sync.ecss.extensions.api.AuthorReferenceResolver;
import ro.sync.ecss.extensions.api.StylesFilter;
import ro.sync.ecss.extensions.api.UniqueAttributesRecognizer;
import ro.sync.ecss.extensions.api.content.ClipboardFragmentProcessor;
import ro.sync.ecss.extensions.api.link.LinkTextResolver;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.dita.map.DITAMapExtensionsBundle;

public class DitaSemiaMapExtensionBundle extends DITAMapExtensionsBundle {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DitaSemiaMapExtensionBundle.class.getName());

	private ProfilingOptionsDialog dialog;
	private boolean active = true;
	
	public DitaSemiaMapExtensionBundle() {
		dialog = DitaSemiaExtensionBundle.getDialog();
		dialog.setMapExtensionBundle(this);
		DitaSemiaExtensionBundle.initDocumentCacheHandler();
	}
	
	@Override
	public String getDescription() {
		return "DITA-SEMIA Map extension bundle";
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
			return isContentReference;
		} else {
			return false;
		}
	}
	
	
	@Override
	public AuthorReferenceResolver createAuthorReferenceResolver() {
		DitaSemiaMapReferenceResolver mapReferenceResolver = new DitaSemiaMapReferenceResolver();
		dialog.setMapReferenceResolver(mapReferenceResolver);
		return mapReferenceResolver;
	}
	
	@Override
	public LinkTextResolver createLinkTextResolver() {
		DitaSemiaLinkTextResolver mapLinkTextResolver = new DitaSemiaLinkTextResolver();
		dialog.setMapLinkTextResolver(mapLinkTextResolver);
		return mapLinkTextResolver;
	}
	
	
	@Override
	public StylesFilter createAuthorStylesFilter() {
		DitaSemiaStylesFilter mapStylesFilter = new DitaSemiaStylesFilter();
		dialog.setMapStylesFilter(mapStylesFilter);
		return mapStylesFilter;
	}

	private DitaSemiaUniqueAttributesRecognizer uniqueAttributesRecognizer;

	@Override
	public AuthorExtensionStateListener createAuthorExtensionStateListener() {
		uniqueAttributesRecognizer = new DitaSemiaUniqueAttributesRecognizer();
		dialog.setMapUniqueAttributesRecognizer(uniqueAttributesRecognizer);
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
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return active;
	}
}
