/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Oxygen;

import org.DitaSemia.Base.XsltConref.XsltConref;

import ro.sync.ecss.extensions.api.AuthorReferenceResolver;
import ro.sync.ecss.extensions.api.StylesFilter;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.dita.map.DITAMapExtensionsBundle;

public class DitaSemiaMapExtensionBundle extends DITAMapExtensionsBundle {

	@Override
	public String getDescription() {
		return "DITA-SEMIA Map extension bundle";
	}
	
	
	@Override
	public boolean isContentReference(AuthorNode node) {
		boolean isContentReference = false;
		if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null))) {
			isContentReference = true;
		} else {
			isContentReference = super.isContentReference(node);
		}
		return isContentReference;
	}
	
	
	@Override
	public AuthorReferenceResolver createAuthorReferenceResolver() {
		return new DitaSemiaMapReferenceResolver();
	}
	
	
	/*@Override
	public StylesFilter createAuthorStylesFilter() {
		return new DitaSemiaStylesFilter();
	}*/

}
